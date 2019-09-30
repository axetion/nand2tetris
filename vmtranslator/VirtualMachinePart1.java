/* Hack VM Translator */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class VirtualMachinePart1 {
	/**
	 * ALGORITHM:
	 * get input file name
	 * create output file name and stream
	 * do one pass to output translated VM code to assembly
	 * close output file stream
	 * if an error occurred and we didn't finish translating, delete the partial output unless the file was already there
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

		try {
			final Parser parser = new Parser(new File(inputFileName));

			/* Check for an extension. */
			int extensionIndex = inputFileName.lastIndexOf(".");

			if (extensionIndex == -1) {  // no extension
				outputFileName = inputFileName + ".asm";
			} else {  // trim off extension
				outputFileName = inputFileName.substring(0, extensionIndex) + ".asm";
			}

			final File outputFile = new File(outputFileName);
			final boolean alreadyExisted = outputFile.exists();  // check if the file already existed so we don't delete it on error

			final CodeWriter writer = new CodeWriter(outputFile);

			/* This gets called on System.exit (error). */
			Thread shutdownHook = new Thread() {
				@Override
				public void run() {
					parser.close();
					writer.close();

					if (!alreadyExisted) {
						outputFile.delete();
					}
				}
			};

			Runtime runtime = Runtime.getRuntime();
			runtime.addShutdownHook(shutdownHook);

			/* Actually translate the code. */
			VirtualMachinePart1.translate(parser, writer);

			/* Remove shutdown hook so it doesn't delete anything when we return normally after this. */
			runtime.removeShutdownHook(shutdownHook);
			parser.close();
			writer.close();
		} catch (FileNotFoundException ex) {
			System.err.printf("VMTranslator: error opening %s%n", ex.getMessage());
			System.exit(1);
		}
	}

	/**
	 * DESCRIPTION: translates the VM code contained in the input file from provided parser, and outputs to the output file from provided CodeWriter
	 * PRECONDITION: input and output files both opened, input stream is at the beginning of the file
	 * POSTCONDITION: output file contains translated assembly
	 */
	private static void translate(Parser parser, CodeWriter writer) {
		while (parser.hasMoreCommands()) {
			parser.advance();

			if (!parser.empty()) {  // ignore empty lines (or maybe a line that had a comment)
				try {
					writer.emit(parser.getCommand(), parser.getArgs());  // get the command and args and pass to CodeWriter
				} catch (TranslationException ex) {  // error during translation?
					System.err.printf("VMTranslator: %s:%d: %s%n~>\t%s%n", parser.getFileName(), parser.getLineNumber(), ex.getMessage(), parser.getLine());
					System.exit(1);
				}
			}
		}
	}
}