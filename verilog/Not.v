//-----------------------------------------------------
// Design Name : Not
// File Name   : Not.v
// Function    : Not chip (parameterized)
//-----------------------------------------------------
module Not #(parameter WORD = 1) (input[WORD - 1 : 0] in, output[WORD - 1 : 0] out);
	wire [WORD - 1 : 0] out;
	assign out = ~in;
endmodule
