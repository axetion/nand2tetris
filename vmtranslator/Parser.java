/* Hack VM Translator */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {
	private Scanner inputFile;
	private String filename;
	private int lineNumber = 0;

	private String rawLine;
	private String cleanLine;
	private String[] args;

	/**
	 * DESCRIPTION: opens input file/stream and prepares to parse
	 * PRECONDITION: provided file exists and is VM file
	 * POSTCONDITION: if file can't be opened, throws FileNotFoundException
	 */
	public Parser(File file) throws FileNotFoundException {
		filename = file.getName();
		inputFile = new Scanner(file);
	}

	/**
	 * DESCRIPTION: returns boolean if more commands left
	 * PRECONDITION: file stream is open
	 * POSTCONDITION: returns true if more commands
	 */
	public boolean hasMoreCommands() {
		return inputFile.hasNextLine();
	}

	/**
	 * DESCRIPTION: closes input stream
	 * PRECONDITION: file was opened
	 * POSTCONDITION: file is closed
	 */
	public void close() {
		inputFile.close();
	}

	/**
	 * DESCRIPTION: reads next line from file and splits it up into the command and arguments
	 * PRECONDITION: file stream is open, called only if hasMoreCommands()
	 * POSTCONDITION: current instruction parts put into args
	 */
	public void advance() {
		rawLine = inputFile.nextLine();
		lineNumber++;

		cleanLine();
		parse();
	}

	/**
	 * DESCRIPTION: strips comments, trailing whitespace, and lower cases the current line
	 * PRECONDITION: rawLine contains the current line (advance() was called)
	 * POSTCONDITION: cleanLine contains the cleaned line for parsing
	 */
	private void cleanLine() {
		cleanLine = rawLine.replaceAll("//.*$", "").trim().toLowerCase();
	}

	/**
	 * DESCRIPTION: splits the cleaned line on spaces
	 * PRECONDITION: cleanLine contains the current line (advance() and cleanLine() were called)
	 * POSTCONDITION: if the line was not empty, args[0] contains the command and the arguments follow it
	 */
	private void parse() {
		args = cleanLine.split(" ");
	}

	/**
	 * DESCRIPTION: returns the name of the parsed command
	 * PRECONDITION: parse() was called and cleanLine was not empty
	 * POSTCONDITION: returns the command
	 */
	public String getCommand() {
		return args[0];
	}

	/**
	 * DESCRIPTION: returns the arguments of the parsed command
	 * PRECONDITION: parse() was called and cleanLine was not empty
	 * POSTCONDITION: returns the arguments
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * DESCRIPTION: getter for rawLine from file (debugging)
	 * PRECONDITION: advance() was called to put value from file in here
	 * POSTCONDITION: returns string of current original line from file
	 */
	public String getLine() {
		return rawLine;
	}

	/**
	 * DESCRIPTION: checks whether the line was empty (e.g. just whitespace or a comment)
	 * PRECONDITION: advance() and cleanLine were called
	 * POSTCONDITION: returns true if the line is empty and false otherwise
	 */
	public boolean empty() {
		return cleanLine.isEmpty();
	}

	/**
	 * DESCRIPTION: getter for lineNumber (debugging)
	 * PRECONDITION: N/A
	 * POSTCONDITION: returns line number currently being processed from file
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * DESCRIPTION: getter for file name (debugging)
	 * PRECONDITION: file was opened
	 * POSTCONDITION: returns the name of the file being read from
	 */
	public String getFileName() {
		return filename;
	}
}