//-----------------------------------------------------
// Design Name : And
// File Name   : And.v
// Function    : And chip (parameterized)
//-----------------------------------------------------
module And #(parameter WORD = 1) (input[WORD - 1 : 0] a, input[WORD - 1 : 0] b,
								  output[WORD - 1 : 0] out);
	wire [WORD - 1 : 0] out;
	assign out = a & b;
endmodule