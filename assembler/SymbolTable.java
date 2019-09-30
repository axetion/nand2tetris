/* Hack Assembler for CS 220 */

import java.util.HashMap;

public class SymbolTable {
	private static final String INITIAL_VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";
	private static final String ALL_VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";
	private final HashMap<String, Integer> symbolTable = new HashMap<String, Integer>();

	/**
	 * DESCRIPTION: initializes hashmap with predefined symbols
	 * PRECONDITION: follows symbols/values from book/appendix
	 * POSTCONDITION: all hashmap values have valid address integer
	 */
	public SymbolTable() {
		for (int i = 0; i < 16; i++) {
			symbolTable.put("R" + i, i);
		}

		symbolTable.put("SP", 0);
		symbolTable.put("LCL", 1);
		symbolTable.put("ARG", 2);
		symbolTable.put("THIS", 3);
		symbolTable.put("THAT", 4);

		symbolTable.put("SCREEN", 0x4000);
		symbolTable.put("KBD", 0x6000);
	}

	/**
	 * DESCRIPTION: adds new pair of symbol/address to hashmap
	 * PRECONDITION: symbol/address pair not in hashmap (checks contains() 1st)
	 * POSTCONDITION: adds pair, returns 0 if added, or the offending character if illegal name -A
	 */
	public char addEntry(String symbol, int address) {
		char invalid = isValidName(symbol);

		if (invalid != 0) {
			return invalid;
		}

		symbolTable.put(symbol, address);
		return 0;
	}

	/**
	 * DESCRIPTION: returns boolean of whether hashmap has symbol or not
	 * PRECONDITION: table has been initialized
	 * POSTCONDITION: returns boolean if arg is in table or not
	 */
	public boolean contains(String symbol) {
		return symbolTable.containsKey(symbol);
	}

	/**
	 * DESCRIPTION: returns address in hashmap of given symbol
	 * PRECONDITION: symbol is in hashmap (check w/ contains() first)
	 * POSTCONDITION: returns address associated with symbol in hashmap
	 */
	public int getAddress(String symbol) {
		Integer address = symbolTable.get(symbol);
		return (address == null ? -1 : address);
	}

	/**
	 * DESCRIPTION: checks if symbol is valid identifier
	 * PRECONDITION: symbol is not empty
	 * POSTCONDITION: returns 0 if symbol is valid, or the offending if illegal name
	 */
	private char isValidName(String symbol) {
		char[] chars = symbol.toCharArray();

		if (INITIAL_VALID_CHARS.indexOf(chars[0]) == -1) {
			return chars[0];
		}

		for (int i = 1; i < chars.length; i++) {
			if (ALL_VALID_CHARS.indexOf(chars[i]) == -1) {
				return chars[i];
			}
		}

		return 0;
	}
}