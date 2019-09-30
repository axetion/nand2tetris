/* Hack Assembler for CS 220 */

import java.util.HashMap;

public class Code {
	private final HashMap<String, String> compCodes = new HashMap<String, String>();
	private final HashMap<String, String> destCodes = new HashMap<String, String>();
	private final HashMap<String, String> jumpCodes = new HashMap<String, String>();

	/**
	 * DESCRIPTION: initializes hashmap with all valid translations for assembly mnemonics
	 * PRECONDITION: follows Hack instruction format from book/appendix
	 * POSTCONDITION: all valid assembly mnemonics are present in hashmap
	 */
	public Code() {
		compCodes.put(null, "0000000");
		compCodes.put("0", "0101010");
		compCodes.put("1", "0111111");
		compCodes.put("-1", "0111010");

		compCodes.put("d", "0001100");
		compCodes.put("a", "0110000");
		compCodes.put("m", "1110000");

		compCodes.put("!d", "0001101");
		compCodes.put("!a", "0110001");
		compCodes.put("!m", "1110001");

		compCodes.put("-d", "0001111");
		compCodes.put("-a", "0110011");
		compCodes.put("-m", "1110011");

		compCodes.put("d+1", "0011111");
		compCodes.put("a+1", "0110111");
		compCodes.put("m+1", "1110111");

		compCodes.put("d-1", "0001110");
		compCodes.put("a-1", "0110010");
		compCodes.put("m-1", "1110010");

		compCodes.put("d+a", "0000010");
		compCodes.put("a+d", "0000010");
		compCodes.put("d+m", "1000010");
		compCodes.put("m+d", "1000010");

		compCodes.put("d-a", "0010011");
		compCodes.put("d-m", "1010011");

		compCodes.put("a-d", "0000111");
		compCodes.put("m-d", "1000111");

		compCodes.put("d&a", "0000000");
		compCodes.put("a&d", "0000000");
		compCodes.put("d&m", "1000000");
		compCodes.put("m&d", "1000000");

		compCodes.put("d|a", "0010101");
		compCodes.put("a|d", "0010101");
		compCodes.put("d|m", "1010101");
		compCodes.put("m|d", "1010101");

		destCodes.put(null, "000");
		destCodes.put("m", "001");
		destCodes.put("d", "010");
		destCodes.put("a", "100");

		destCodes.put("dm", "011");
		destCodes.put("md", "011");

		destCodes.put("am", "101");
		destCodes.put("ma", "101");

		destCodes.put("ad", "110");
		destCodes.put("da", "110");

		destCodes.put("adm", "111");
		destCodes.put("amd", "111");
		destCodes.put("mad", "111");
		destCodes.put("mda", "111");
		destCodes.put("dma", "111");
		destCodes.put("dam", "111");

		jumpCodes.put(null, "000");
		jumpCodes.put("jgt", "001");
		jumpCodes.put("jeq", "010");
		jumpCodes.put("jlt", "100");
		jumpCodes.put("jge", "011");
		jumpCodes.put("jne", "101");
		jumpCodes.put("jle", "110");
		jumpCodes.put("jmp", "111");
	}

	/**
	 * DESCRIPTION: returns C-instruction bits for comp mnemonic
	 * PRECONDITION: compCodes hashmap has been initialized
	 * POSTCONDITION: returns string containing C bits or null if mnemonic was invalid
	 */
	public String getComp(String mnemonic) {
		return compCodes.get(mnemonic);
	}

	/**
	 * DESCRIPTION: returns C-instruction bits for dest mnemonic
	 * PRECONDITION: destCodes hashmap has been initialized
	 * POSTCONDITION: returns string containing C bits or null if mnemonic was invalid
	 */
	public String getDest(String mnemonic) {
		return destCodes.get(mnemonic);
	}

	/**
	 * DESCRIPTION: returns C-instruction bits for jump mnemonic
	 * PRECONDITION: jumpCodes hashmap has been initialized
	 * POSTCONDITION: returns string containing C bits or null if mnemonic was invalid
	 */
	public String getJump(String mnemonic) {
		return jumpCodes.get(mnemonic);
	}

	/**
	 * DESCRIPTION: converts number to 16-bit word with padding, after truncating
	 * PRECONDITION: n/a
	 * POSTCONDITION: returns 16-bit binary representation of input as string
	 */
	public String decimalToBinary(int decimal) {
		StringBuilder out = new StringBuilder();
		decimal = decimal & 0xffff;

		for (int i = 0; i < 16; i++) {
			out.append((decimal & 1) == 0 ? '0' : '1');
			decimal >>= 1;
		}

		return out.reverse().toString();
	}
}