/* Hack Assembler for CS 220 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Assembler {
	/**
	 * ALGORITHM:
	 * get input file name
	 * create output file name and stream
	 * create symbol table
	 * do first pass to build symbol table (no output yet!)
	 * do second pass to output translated ASM to HACK code
	 * close output file stream
	 * if an error occurred and we didn't finish translating, delete the partial output unless the file was already there -A
	 */
	public static void main(String[] args) {
		String inputFileName, outputFileName;
	
		/* Get input file name from command line or console input. */
		if (args.length == 1) {
			inputFileName = args[0];
		} else {
			Scanner keyboard = new Scanner(System.in);
			inputFileName = keyboard.nextLine();

			keyboard.close();
		}

		/* Check for an extension. */
		int extensionIndex = inputFileName.lastIndexOf(".");

		if (extensionIndex == -1) {  // no extension
			outputFileName = inputFileName + ".hack";
		} else {  // trim off extension
			outputFileName = inputFileName.substring(0, extensionIndex) + ".hack";
		}

		final File outputFile = new File(outputFileName);
		final boolean alreadyExisted = outputFile.exists();  // check if the file already existed so we don't delete it on error

		try {
			final PrintWriter outputWriter = new PrintWriter(outputFile);
			SymbolTable symbolTable = new SymbolTable();

			/* This gets called on System.exit (error). */
			Thread shutdownHook = new Thread() {
				@Override
				public void run() {
					outputWriter.close();

					if (!alreadyExisted) {
						outputFile.delete();
					}
				}
			};

			Runtime runtime = Runtime.getRuntime();
			runtime.addShutdownHook(shutdownHook);

			/* First pass: resolve labels. */
			Assembler.firstPass(inputFileName, symbolTable);

			/* Second pass: emit machine code. */
			Assembler.secondPass(inputFileName, symbolTable, outputWriter);

			/* Remove shutdown hook so it doesn't delete anything when we return normally after this. */
			runtime.removeShutdownHook(shutdownHook);
			outputWriter.close();
		} catch (FileNotFoundException ex) {
			System.err.printf("Assembler: error opening %s%n", ex.getMessage());
			System.exit(1);
		}
	}

	/**
	 * DESCRIPTION: parses labels and adds them to the symbol table
	 * PRECONDITION: symbol table initialized but empty aside from builtin symbols
	 * POSTCONDITION: symbol table contains ROM addresses for each label in the file
	 */
	private static void firstPass(String inputFileName, SymbolTable symbolTable) {
		Parser parser = new Parser(inputFileName);
		int romAddress = 0;

		while (parser.hasMoreCommands()) {
			/* Advance parser and check for a label. */
			parser.advance();

			if (parser.getCommandType() != Parser.L_COMMAND) {  // not interested in non labels
				if (parser.getCommandType() != Parser.NO_COMMAND) {  // ...unless they're a valid A or C command, in which case we advance the PC
					romAddress++;
				}

				continue;
			}

			/* Get the symbol and trim the ending parenthesis. */
			String symbol = parser.getSymbol();
			int end = symbol.length() - 1;

			if (symbol.isEmpty() || symbol.charAt(end) != ')') {
				System.err.printf("%s:%d: error: unterminated label:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), parser.getRawLine());
				System.exit(1);
			}

			symbol = symbol.substring(0, end);

			/* Add label to symbol table for next pass. */
			char c = symbolTable.addEntry(symbol, romAddress);

			if (c != 0) {
				System.err.printf("%s:%d: error: character '%c' not allowed in label:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), c, parser.getRawLine());
				System.exit(1);
			}
		}
	}

	/**
	 * DESCRIPTION: translates A and C instructions and emits to the output file
	 * PRECONDITION: symbol table initialized with all the labels in the file, output file is open
	 * POSTCONDITION: file is populated with machine-code versions of each A and C instruction
	 */
	private static void secondPass(String inputFileName, SymbolTable symbolTable, PrintWriter outputFile) {
		Parser parser = new Parser(inputFileName);

		Code code = new Code();
		int dataSegmentPos = 16;  // beginning of scratch RAM
		int lastUsedAddress = 0;  // for checking out of bounds memory access

		while (parser.hasMoreCommands()) {
			/* Advance parser and switch on different commands. */
			parser.advance();

			switch (parser.getCommandType()) {
			case Parser.A_COMMAND:  // A register load
				String symbol = parser.getSymbol();

				if (symbol.isEmpty()) {
					System.err.printf("%s:%d: error: unterminated A-instruction:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), parser.getRawLine());
					System.exit(1);
				}

				try {
					lastUsedAddress = Integer.parseInt(symbol);

					/* Symbol was valid integer, use as address directly. */

					if (lastUsedAddress > 0x7fff) {  // 0x7ffff = 32768
						System.err.printf("%s:%d: error: A-Register load out of range:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), parser.getRawLine());
						System.exit(1);
					} else if (lastUsedAddress < 0) {  // negative loads not allowed
						System.err.printf("%s:%d: error: A-Register load out of range (try loading the positive number and inverting):%n~>\t| %s%n", inputFileName, parser.getLineNumber(), parser.getRawLine());
						System.exit(1);
					}
				} catch (NumberFormatException ex) {
					/* Symbol was not a valid integer. Create in symbol table if it doesn't already exist. */
					lastUsedAddress = symbolTable.getAddress(symbol);

					if (lastUsedAddress == -1) {
						char c = symbolTable.addEntry(symbol, dataSegmentPos);

						if (c != 0) {
							System.err.printf("%s:%d: error: character '%c' not allowed in symbol:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), c, parser.getRawLine());
							System.exit(1);
						}

						lastUsedAddress = dataSegmentPos++;  // advance position in scratch RAM for next variable created

						if (lastUsedAddress > 0x4000) {
							System.err.printf("%s:%d: error: not enough space to allocate variable:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), parser.getRawLine());
							System.exit(1);
						}
					}
				}

				/* Emit resulting address. */
				outputFile.println(code.decimalToBinary(lastUsedAddress));
				break;
			case Parser.C_COMMAND:  // Normal instruction
				/* Get each split up component of the instruction (dest = comp; jump). */
				String dest = parser.getDest();
				String comp = parser.getComp();
				String jump = parser.getJump();

				/* Are we reading from memory for this instruction? */
				if (comp != null && comp.contains("m")) {
					if (comp.contains("a")) {  // Can't read A and M at the same time.
						System.err.printf("%s:%d: error: cannot load A and M in the same instruction:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), parser.getRawLine());
						System.exit(1);
					} else if (lastUsedAddress > 0x6000) {  // Last thing loaded into the A register went beyond the keyboard. 0x6000 = 24576
						System.err.printf("%s:%d: warning: possible out-of-bounds memory access:%n\t| @%d%n\t| ...%n~>\t| %s%n", inputFileName, parser.getLineNumber(), lastUsedAddress, parser.getRawLine());
					}
				}

				if (dest != null) {
					/* Are we writing to memory for this instruction? */
					if (dest.contains("m")) {
						if (lastUsedAddress > 0x6000) {  // Last thing loaded into the A register went beyond the keyboard. 0x6000 = 24576
							System.err.printf("%s:%d: warning: possible out-of-bounds memory access:%n\t| @%d%n\t| ...%n~>\t| %s%n", inputFileName, parser.getLineNumber(), lastUsedAddress, parser.getRawLine());
						} else if (lastUsedAddress == 0x6000) {  // Keyboard is fine to read from but not write.
							System.err.printf("%s:%d: warning: writing to KBD is a no-op:%n\t| @%d%n\t| ...%n~>\t| %s%n", inputFileName, parser.getLineNumber(), lastUsedAddress, parser.getRawLine());
						}
					}

					/* Reset our last A-register load if we're writing to A here. */
					if (dest.contains("a")) {
						lastUsedAddress = 0;
					}
				}

				/* Get the appropriate machine code bits for each mnemonic. */
				String compBits = code.getComp(comp);

				if (compBits == null) {
					System.err.printf("%s:%d: error: invalid instruction:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), parser.getRawLine());
					System.exit(1);
				}

				String destBits = code.getDest(dest);

				if (destBits == null) {
					System.err.printf("%s:%d: error: invalid destination:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), parser.getRawLine());
					System.exit(1);
				}

				String jumpBits = code.getJump(jump);

				if (jumpBits == null) {
					System.err.printf("%s:%d: error: invalid jump:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), parser.getRawLine());
					System.exit(1);
				}

				if (comp != null) {
					if (dest == null && jump == null) {  // Comp isn't null, but we don't jump or store the result anywhere. No-op.
						System.err.printf("%s:%d: warning: no-op:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), parser.getRawLine());
					} else if (dest != null && jump != null) {  // My CPU and the hardware simulator can handle writing to a register and jumping at the same time, but not all may be capable.
						System.err.printf("%s:%d: warning: jump and dest in the same instruction is non-standard:%n~>\t| %s%n", inputFileName, parser.getLineNumber(), parser.getRawLine());
					}
				}

				/* Pad to 16 bits and emit to file. */
				outputFile.printf("111%s%s%s%n", compBits, destBits, jumpBits);
				break;
			default:  // Either a label or a blank line. Ignore.
				continue;
			}
		}
	}
}