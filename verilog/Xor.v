//-----------------------------------------------------
// Design Name : Xor
// File Name   : Xor.v
// Function    : Xor chip (parameterized)
//-----------------------------------------------------
module Xor #(parameter WORD = 1) (input[WORD - 1 : 0] a, input[WORD - 1 : 0] b,
								  output[WORD - 1 : 0] out);
	wire [WORD - 1 : 0] out;
	assign out = a ^ b;
endmodule