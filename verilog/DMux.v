//-----------------------------------------------------
// Design Name : DMux
// File Name   : DMux.v
// Function    : DMux chip
//-----------------------------------------------------
module DMux (input in, input sel,
			 output a, output b);
	wire a, b;

	assign a = in & ~sel;
	assign b = in & sel;
endmodule