/* Hack Assembler for CS 220 */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Parser {
	public static final char NO_COMMAND = 'N';
	public static final char A_COMMAND = 'A';
	public static final char C_COMMAND = 'C';
	public static final char L_COMMAND = 'L';

	private Scanner inputFile;
	private int lineNumber;

	private String rawLine;
	private String cleanLine;

	private char commandType;
	private String symbol;
	private String destMnemonic;
	private String compMnemonic;
	private String jumpMnemonic;

	/**
	 * DESCRIPTION: opens input file/stream and prepares to parse
	 * PRECONDITION: provided file is ASM file
	 * POSTCONDITION: if file can't be opened, ends program w/ error message
	 */
	public Parser(String inFileName) {
		try {
			inputFile = new Scanner(new FileReader(inFileName));
		} catch (FileNotFoundException ex) {
			System.err.printf("Assembler: error opening %s%n", ex.getMessage());
			System.exit(1);
		}
	}

	/**
	 * DESCRIPTION: returns boolean if more commands left, closes stream if not
	 * PRECONDITION: file stream is open
	 * POSTCONDITION: returns true if more commands, else closes stream
	 */
	public boolean hasMoreCommands() {
		if (inputFile.hasNextLine()) {
			return true;
		} else {
			inputFile.close();
			return false;
		}
	}

	/**
	 * DESCRIPTION: reads next line from file and parses it into instance vars
	 * PRECONDITION: file stream is open, called only if hasMoreCommands()
	 * POSTCONDITION: current instruction parts put into instance vars
	 */
	public void advance() {
		rawLine = inputFile.nextLine();
		lineNumber++;

		cleanLine();
		parse();
	}

	/**
	 */
	private void cleanLine() {
		cleanLine = rawLine.replaceAll("\\s|//.*$", "");
		commandType = 0;  // reset command type so it'll be parsed again on next getCommandType() call
	}

	private void parseCommandType() {
		if (cleanLine.isEmpty()) {
			commandType = NO_COMMAND;
			return;
		}

		switch (cleanLine.charAt(0)) {
		case '@':
			commandType = A_COMMAND;
			break;
		case '(':
			commandType = L_COMMAND;
			break;
		default:
			commandType = C_COMMAND;
		}
	}

	/**
	 * DESCRIPTION: getter for command type
	 * PRECONDITION: cleanLine has been parsed (advance was called)
	 * POSTCONDITION: returns char for command type (N/A/C/L)
	 */
	public char getCommandType() {
		if (commandType == 0) {
			parseCommandType();
		}

		return commandType;
	}

	/**
	 * DESCRIPTION: helper method parses line depending on instruction type
	 * PRECONDITION: advance() called so cleanLine has value
	 * POSTCONDITION: appropriate parts (instance vars) of instruction filled
	 */
	private void parse() {
		switch (getCommandType()) {
		case A_COMMAND:  // fall through to parseSymbol()
		case L_COMMAND:
			parseSymbol();
			break;
		case C_COMMAND:
			parseDest();
			parseComp();
			parseJump();
			break;
		}
	}

	/**
	 * DESCRIPTION: parse symbol for A- or L-commands
	 * PRECONDITION: advance() called so cleanLine has value, call for A- and L-commands only
	 * POSTCONDITION: symbol has appropriate value from instruction assigned
	 */
	private void parseSymbol() {
		symbol = cleanLine.substring(1);
	}

	/**
	 * DESCRIPTION: getter for command type
	 * PRECONDITION: cleanLine has been parsed (advance was called), call for labels only (use getCommandType())
	 * POSTCONDITION: returns string for symbol name
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * DESCRIPTION: helper method parses line to get dest part
	 * PRECONDITION: advance() called so cleanLine value, call for C-instruction only
	 * POSTCONDITION: destMnemonic set to appropriate value from instruction ---- or null if instruction does not have a destination -A
	 */
	private void parseDest() {
		int destTerminator = cleanLine.indexOf('=');

		if (destTerminator == -1) {
			destMnemonic = null;
		} else {
			destMnemonic = cleanLine.substring(0, destTerminator).toLowerCase();
		}
	}

	/**
	 * DESCRIPTION: getter for dest part of C-instruction
	 * PRECONDITION: cleanLine has been parsed (advance was called), call for C-instruction only (use getCommandType())
	 * POSTCONDITION: returns mnemonic (ASM symbol) for dest part
	 */
	public String getDest() {
		return destMnemonic;
	}

	/**
	 * DESCRIPTION: helper method parses line to get comp part
	 * PRECONDITION: advance() called so cleanLine has value, call for C-instruction only
	 * POSTCONDITION: compMnemonic set to appropriate value from instruction
	 */
	private void parseComp() {
		int destTerminator = cleanLine.indexOf('=');
		int compTerminator = cleanLine.indexOf(';');

		if (compTerminator == -1) {
			compMnemonic = cleanLine.substring(destTerminator + 1).toLowerCase();
		} else {
			compMnemonic = cleanLine.substring(destTerminator + 1, compTerminator).toLowerCase();
		}
	}

	/**
	 * DESCRIPTION: getter for comp part of C-instruction
	 * PRECONDITION: cleanLine has been parsed (advance was called), call for C-instruction only (use getCommandType())
	 * POSTCONDITION: returns mnemonic (ASM symbol) for comp part
	 */
	public String getComp() {
		return compMnemonic;
	}

	/**
	 * DESCRIPTION: helper method parses line to get jump part
	 * PRECONDITION: advance() called so cleanLine has value, call for C-instruction only
	 * POSTCONDITION: jumpMnemonic set to appropriate value from instruction ---- or null if instruction does not have a jump -A
	 */
	private void parseJump() {
		int compTerminator = cleanLine.indexOf(';');

		if (compTerminator == -1) {
			jumpMnemonic = null;
		} else {
			jumpMnemonic = cleanLine.substring(compTerminator + 1).toLowerCase();
		}
	}

	/**
	 * DESCRIPTION: getter for jump part of C-instruction
	 * PRECONDITION: cleanLine has been parsed (advance was called), call for C-instruction only (use getCommandType())
	 * POSTCONDITION: returns mnemonic (ASM symbol) for jump part
	 */
	public String getJump() {
		return jumpMnemonic;
	}

	/**
	 * DESCRIPTION: getter for string version of command type (debugging)
	 * PRECONDITION: advance() and parse() have been called
	 * POSTCONDITION: returns string version of command type
	 */
	public String getCommandTypeString() {
		return Character.toString(commandType);
	}

	/**
	 * DESCRIPTION: getter for rawLine from file (debugging)
	 * PRECONDITION: advance() was called to put value from file in here
	 * POSTCONDITION: returns string of current original line from file
	 */
	public String getRawLine() {
		return rawLine;
	}

	/**
	 * DESCRIPTION: getter for cleanLine from file  (debugging)
	 * PRECONDITION: advance() and cleanLine() have been called
	 * POSTCONDITION: returns string of current clean instruction from file
	 */
	public String getCleanLine() {
		return cleanLine;
	}

	/**
	 * DESCRIPTION: getter for lineNumber (debugging)
	 * PRECONDITION: N/A
	 * POSTCONDITION: returns line number currently being processed from file
	 */
	public int getLineNumber() {
		return lineNumber;
	}
}
