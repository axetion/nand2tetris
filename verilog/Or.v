//-----------------------------------------------------
// Design Name : Or
// File Name   : Or.v
// Function    : Or chip (parameterized)
//-----------------------------------------------------
module Or #(parameter WORD = 1) (input[WORD - 1 : 0] a, input[WORD - 1 : 0] b,
								  output[WORD - 1 : 0] out);
	wire [WORD - 1 : 0] out;
	assign out = a | b;
endmodule