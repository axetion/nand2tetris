/* Hack VM Translator */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

/* Represents an exception during translation. */
class TranslationException extends Exception {
	private String message;

	/**
	 * DESCRIPTION: sets the error message associated with the exception, to be displayed later
	 * PRECONDITION: N/A
	 * POSTCONDITION: message contains the provided error message
	 */
	TranslationException(String errorMsg) {
		message = errorMsg;
	}

	/**
	 * DESCRIPTION: getter for the error message
	 * PRECONDITION: message was initialized
	 * POSTCONDITION: returns the human-readable error message
	 */
	@Override
	public String getMessage() {
		return message;
	}
}

/* Represents a VM command (push, add, etc.). Responsible for emitting assembly code. */
abstract class Emitter {
	private int numArgs;

	/**
	 * DESCRIPTION: takes in the expected number of arguments (arity) for this command
	 * PRECONDITION: N/A
	 * POSTCONDITION: numArgs contains the arity + 1 (to account for the leading command name)
	 */
	Emitter(int arity) {
		numArgs = arity + 1;
	}

	/**
	 * DESCRIPTION: checks the number of arguments passed, then delegates to doEmit
	 * PRECONDITION: numArgs was initialized
	 * POSTCONDITION: doEmit is called (what it does is the responsibility of the inheriting class)
	 */
	void emit(String[] args) throws TranslationException {
		if (args.length != numArgs) {
			throw new TranslationException(String.format("expected %d arguments; got %d", numArgs - 1, args.length - 1));
		}

		doEmit(args);
	}

	/**
	 * DESCRIPTION: emits assembly code for the given command and arguments
	 * PRECONDITION: N/A
	 * POSTCONDITION: assembly code associated with this VM command was emitted in an implementation-defined manner
	 */
	protected abstract void doEmit(String[] args) throws TranslationException;
}

public class CodeWriter {
	private final static HashMap<String, String> segments = new HashMap<String, String>();  // Maps names of segments to addresses in assembly
	private final static HashMap<String, String> pointers = new HashMap<String, String>();  // Maps pointer segment indices to addresses in assembly

	/* Initialize these hashmaps only once. They are shared across all CodeWriter instances. */
	static {
		segments.put("local", "LCL");
		segments.put("argument", "ARG");
		segments.put("this", "THIS");
		segments.put("that", "THAT");

		pointers.put("0", "THIS");
		pointers.put("1", "THAT");
	}

	private final HashMap<String, Emitter> commands = new HashMap<String, Emitter>();  // Maps names of commands to Emitter classes
	private final String currentFile;
	private final PrintWriter writer;

	private int labelCounter = 0;
	private boolean isAtTopOfStack = false;  // True if the A register is already pointed at the top of the stack from a previous operation.
	private boolean afterPrologue = true;  // True if nothing has been loaded into the A register since the prologue -- in other words, it contains SP.

	/**
	 * DESCRIPTION: opens the output file, initializes the Emitters for each command and writes the "prologue" assembly code that initializes the stack pointer
	 * PRECONDITION: N/A
	 * POSTCONDITION: commands hash table initialized, output file opened and contains prologue code
	 */
	public CodeWriter(File file) throws FileNotFoundException {
		currentFile = file.getName();
		writer = new PrintWriter(file);

		/* Dyadic (takes two arguments) arthimetic operations */
		commands.put("add", new Emitter(0) {
			@Override
			protected void doEmit(String[] args) {
				emitComment("add");
				emitDyad("M = D + M");
			}
		});

		commands.put("sub", new Emitter(0) {
			@Override
			protected void doEmit(String[] args) {
				emitComment("sub");
				emitDyad("M = M - D");
			}
		});

		commands.put("and", new Emitter(0) {
			@Override
			protected void doEmit(String[] args) {
				emitComment("and");
				emitDyad("M = D & M");
			}
		});

		commands.put("or", new Emitter(0) {
			@Override
			protected void doEmit(String[] args) {
				emitComment("or");
				emitDyad("M = D | M");
			}
		});

		/* Monadic (takes one argument) arthimetic operations */
		commands.put("neg", new Emitter(0) {
			@Override
			protected void doEmit(String[] args) {
				emitComment("neg");
				emitMonad("M = -M");
			}
		});

		commands.put("not", new Emitter(0) {
			@Override
			protected void doEmit(String[] args) {
				emitComment("not");
				emitMonad("M = !M");
			}
		});

		/* Comparison operations */
		commands.put("eq", new Emitter(0) {
			@Override
			protected void doEmit(String[] args) {
				emitComment("eq");
				emitCompare("EQ");
			}
		});

		commands.put("gt", new Emitter(0) {
			@Override
			protected void doEmit(String[] args) {
				emitComment("gt");
				emitCompare("GT");
			}
		});

		commands.put("lt", new Emitter(0) {
			@Override
			protected void doEmit(String[] args) {
				emitComment("lt");
				emitCompare("LT");
			}
		});

		/* Jumps */
		commands.put("label", new Emitter(1) {
			@Override
			protected void doEmit(String[] args) {
				emitLabel(args[1]);
			}
		});

		commands.put("goto", new Emitter(1) {
			@Override
			protected void doEmit(String[] args) {
				String label = args[1];

				emitComment(String.format("goto %s", label));
				writer.printf("@%s%n", label);
				writer.println("0; JMP");  // unconditional jump

				/* A register state is ruined after any jump, regardless of where it goes */
				isAtTopOfStack = false;
				afterPrologue = false;
			}
		});

		commands.put("if-goto", new Emitter(1) {
			@Override
			protected void doEmit(String[] args) {
				String label = args[1];

				emitComment(String.format("if-goto %s", label));

				decrementSP();
				writer.println("D = M");  // load in value to compare
				writer.printf("@%s%n", label);
				writer.println("D; JGE");  // >= 0 as opposed to != -1 is required by the test program. Is this a mistake in the tester?

				/* A register state is reset by decrementSP, so no need to do it explicitly. */
			}
		});

		/* Stack manipulation */
		commands.put("push", new Emitter(2) {
			@Override
			protected void doEmit(String[] args) throws TranslationException {
				String type = args[1];
				String data = args[2];

				emitComment(String.format("push %s %s", type, data));

				switch (type) {
				case "constant":
					emitPush(data);  // push constant directly
					break;
				case "temp":
					try {
						int tempAddress = Integer.parseInt(data);
						emitPushDereference(Integer.toString(5 + tempAddress));  // dereference the given address and push its contents
					} catch (NumberFormatException ex) {
						throw new TranslationException("expected number for temp address");
					}
					break;
				case "static":
					emitPushDereference(String.format("%s.%s", currentFile, data));  // dereference the given static variable address and push its contents
					break;
				case "pointer":
					String pointer = pointers.get(data);

					if (pointer == null) {
						throw new TranslationException("pointer index out of bounds");
					}

					emitPushDereference(pointer);  // push the contents at either THIS or THAT
					break;
				default:
					String segment = segments.get(type);

					if (segment == null) {
						throw new TranslationException(String.format("no such segment '%s'", type));
					}

					emitPushDoubleDereference(segment, data);  // double dereference == first you must get the contents at LCL, then add the offset and dereference again
					break;
				}
			}
		});

		commands.put("pop", new Emitter(2) {
			@Override
			protected void doEmit(String[] args) throws TranslationException {
				String type = args[1];
				String data = args[2];

				emitComment(String.format("pop %s %s", type, data));

				/* basically identical to push */
				switch (type) {
				case "temp":
					try {
						int tempAddress = Integer.parseInt(data);
						emitPop(Integer.toString(5 + tempAddress));
					} catch (NumberFormatException ex) {
						throw new TranslationException("expected number for temp address");
					}
					break;
				case "static":
					emitPop(String.format("%s.%s", currentFile, data));
					break;
				case "pointer":
					String pointer = pointers.get(data);

					if (pointer == null) {
						throw new TranslationException("pointer index out of bounds");
					}

					emitPop(pointer);
					break;
				default:
					String segment = segments.get(type);

					if (segment == null) {
						throw new TranslationException(String.format("no such segment '%s'", type));
					}

					emitPopDereference(segment, data);
					break;
				}
			}
		});

		/* Prologue -- initialize stack pointer to 256. */
		writer.println("@256");
		writer.println("D = A");
		writer.println("@SP");
		writer.println("M = D");
	}

	/**
	 * DESCRIPTION: closes output stream
	 * PRECONDITION: file was opened
	 * POSTCONDITION: file is closed
	 */
	public void close() {
		writer.close();
	}

	/**
	 * DESCRIPTION: emits assembly code for the given command and arguments
	 * PRECONDITION: commands hash table was initialized, output stream opened
	 * POSTCONDITION: assembly code is written to the output file, or a TranslationException is thrown
	 */
	public void emit(String instruction, String[] args) throws TranslationException {
		Emitter command = commands.get(instruction);

		if (command == null) {
			throw new TranslationException(String.format("no such command '%s'", instruction));
		}

		command.emit(args);
	}

	/**
	 * DESCRIPTION: helper to emit a label
	 * PRECONDITION: output stream opened
	 * POSTCONDITION: label written to output file
	 */
	private void emitLabel(String label) {
		writer.printf("(%s)%n", label);

		/* If we arrived at this label via a jump, we have no idea what the A register contains. Better safe than sorry. */
		isAtTopOfStack = false;
	}

	/**
	 * DESCRIPTION: helper to generate a temporary label name
	 * PRECONDITION: N/A
	 * POSTCONDITION: returns a unique label name
	 */
	private String makeTempLabel() {
		return String.format("temp%d", labelCounter++);
	}

	/**
	 * DESCRIPTION: helper to emit a comment (for debugging the emitted assembly)
	 * PRECONDITION: output stream opened
	 * POSTCONDITION: comment written to output file
	 */
	private void emitComment(String comment) {
		writer.printf("// %s%n", comment);
	}

	/**
	 * DESCRIPTION: helper to load the address of SP (0)
	 * PRECONDITION: output stream opened
	 * POSTCONDITION: SP is guaranteed to be in the A register at this point in execution
	 */
	private void loadSPAddress() {
		if (afterPrologue) {  // SP is already in the A register after the prologue
			afterPrologue = false;
		} else {
			writer.println("@SP");
		}
	}

	/**
	 * DESCRIPTION: helper to decrement the stack pointer
	 * PRECONDITION: output stream opened
	 * POSTCONDITION: assembly code to decrement the stack pointer written, A register state is reset
	 */
	private void decrementSP() {
		/* We always must reload the stack pointer in these situations, simply because we have to modify it. */
		loadSPAddress();
		writer.println("AM = M - 1");

		isAtTopOfStack = false;
	}

	/**
	 * DESCRIPTION: helper to increment the stack pointer for pushes
	 * PRECONDITION: output stream opened
	 * POSTCONDITION: assembly code to increment the stack pointer written
	 */
	private void incrementSP() {
		loadSPAddress();
		writer.println("M = M + 1");
		writer.println("A = M - 1");

		isAtTopOfStack = true;
	}

	/**
	 * DESCRIPTION: helper to "peek" the top of the stack -- set the A register to there without moving the stack pointer
	 * PRECONDITION: output stream opened
	 * POSTCONDITION: assembly code to peek the stack written
	 */
	private void peekSP() {
		if (!isAtTopOfStack) {  // Many operations preserve the A register. There is no need to reload the stack pointer in these situations.
			loadSPAddress();
			writer.println("A = M - 1");

			isAtTopOfStack = true;
		}
	}

	/**
	 * DESCRIPTION: helper to emit a monadic operation -- a VM operation that takes in one value and has one output.
	 * PRECONDITION: output stream opened, operation is valid assembly
	 * POSTCONDITION: assembly code for operation written
	 */
	private void emitMonad(String operation) {
		peekSP();  // A register now points to top of stack
		writer.println(operation);  // run operation on M in place -- no point in popping into a temporary
	}

	/**
	 * DESCRIPTION: helper to emit a dyadic operation -- a VM operation that takes in two values and has one output.
	 * PRECONDITION: output stream opened, operation is valid assembly
	 * POSTCONDITION: assembly code for operation written
	 */
	private void emitDyad(String operation) {
		decrementSP();  // decrement SP
		writer.println("D = M");  // pop second argument into D
		writer.println("A = A - 1");  // A points to first argument
		writer.println(operation);  // run operation on M and D and output to where first argument was

		isAtTopOfStack = true;
	}

	/**
	 * DESCRIPTION: helper to emit a comparison operation
	 * PRECONDITION: output stream opened, operation is one of LT, GT, EQ
	 * POSTCONDITION: assembly code for operation written
	 */
	private void emitCompare(String compare) {
		String skip = makeTempLabel();  // make a temporary label

		emitDyad("D = M - D");  // subtract the two arguments

		writer.println("M = -1");  // push a -1 (true)
		writer.printf("@%s%n", skip);  // load in our temporary label
		writer.printf("D; J%s%n", compare);  // compare the result of our subtraction to 0 and jump to the temporary label if true

		isAtTopOfStack = false;  // A register state is dependent on which branch is taken. We can't make any assumptions.

		/* This branch is only taken if the comparison was false. Reload the SP. */
		emitMonad("M = 0");  // push a 0
		emitLabel(skip);  // emit temporary label after the false branch
	}

	/**
	 * DESCRIPTION: helper to push a "constant" (can also be the D register) onto the stack
	 * PRECONDITION: output stream opened, constant is either a number or the D register
	 * POSTCONDITION: assembly code for the push operation written
	 */
	private void emitPush(String constant) {
		switch (constant) {
		case "0":
		case "1":
		case "D":
			/* 0, 1, or D. Can be pushed directly via the ALU. */
			incrementSP();
			writer.printf("M = %s%n", constant);
			break;
		case "2":
			/* 2 is most efficiently made by pushing 1 and then adding 1, rather than using an A register load. */
			incrementSP();
			writer.println("M = 1");
			writer.println("M = M + 1");  // 1 + 1
			break;
		default:
			/* Any other number. Load it into the A register and push. */
			writer.printf("@%s%n", constant);
			writer.println("D = A");
			afterPrologue = false;  // A register ruined

			incrementSP();

			writer.println("M = D");
		}

		/* Does the compiler ever generate "push constant -1"? If so I'd add it to the 0/1/D cases. */
	}

	/**
	 * DESCRIPTION: helper to push the value at a given address onto the stack
	 * PRECONDITION: output stream opened, pointer is valid
	 * POSTCONDITION: assembly code for the push operation written
	 */
	private void emitPushDereference(String pointer) {
		/* Load contents into D, then do a normal push with the D register */
		writer.printf("@%s%n", pointer);
		writer.println("D = M");

		afterPrologue = false;
		emitPush("D");
	}

	/**
	 * DESCRIPTION: helper to push a value from a memory segment onto the stack
	 * PRECONDITION: output stream opened, segment and offset are valid
	 * POSTCONDITION: assembly code for the push operation written
	 */
	private void emitPushDoubleDereference(String segment, String offset) {
		/* Load the pointer to the pointer into A */
		writer.printf("@%s%n", segment);
		afterPrologue = false;

		switch (offset) {
		case "0":  // Offset of 0 means we can load the pointer to the segment directly, no math needed 
			writer.println("A = M");
			break;
		case "1":  // Add 1 to the pointer in the same instruction
			writer.println("A = M + 1");
			break;
		case "2":  // Once again, an offset of 2 is most efficiently made by adding 1 twice
			writer.println("A = M + 1");
			writer.println("A = A + 1");
			break;
		default:  // Offset > 2. Load the offset into A and add to the pointer
			writer.println("D = M");
			writer.printf("@%s%n", offset);
			writer.println("A = D + A");
		}

		/* Normal dereference push from here. */
		writer.println("D = M");
		emitPush("D");
	}

	/**
	 * DESCRIPTION: helper to pop a value from the stack to a given address
	 * PRECONDITION: output stream opened, pointer is valid
	 * POSTCONDITION: assembly code for the pop operation written
	 */
	private void emitPop(String pointer) {
		decrementSP();  // decrement SP and retrieve the value there
		writer.println("D = M");
		writer.printf("@%s%n", pointer);  // load position to write to
		writer.println("M = D");
	}

	/**
	 * DESCRIPTION: helper to pop a value from the stack to a given memory segment
	 * PRECONDITION: output stream opened, segment and offset are valid
	 * POSTCONDITION: assembly code for the pop operation written
	 */
	private void emitPopDereference(String pointer, String offset) {
		switch (offset) {
			case "0":  // If offset is 0, we can once again load the position of the memory segment directly
				decrementSP();
				writer.println("D = M");  // pop value

				writer.printf("@%s%n", pointer);
				writer.println("A = M");
				break;
			case "1":  // add 1 to position of memory segment when loading
				decrementSP();
				writer.println("D = M");

				writer.printf("@%s%n", pointer);
				writer.println("A = M + 1");
				break;
			case "2":  // add 1 twice to position of memory segment when loading
				decrementSP();
				writer.println("D = M");

				writer.printf("@%s%n", pointer);
				writer.println("A = M + 1");
				writer.println("A = A + 1");
				break;
			default:
				/* Offset greater than 2. This gets complicated... First compute the address to write to in D (*pointer + offset) */
				writer.printf("@%s%n", offset);  // load offset into A
				writer.println("D = A");
				writer.printf("@%s%n", pointer);
				writer.println("D = D + M");

				/* Pop into D, then swap A and D. This is a massive eyesore, but it's much faster than writing to a temporary in memory. */
				decrementSP();
				writer.println("D = D + M");  // D = top of stack + address
				writer.println("A = D - M");  // A = (top of stack + address) - top of stack = address
				writer.println("D = D - A");  // D = (top of stack + address) - address = top of stack
				break;
		}

		writer.println("M = D");
	}
}