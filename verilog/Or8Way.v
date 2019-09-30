//-----------------------------------------------------
// Design Name : Or8Way
// File Name   : Or8Way.v
// Function    : Or8Way
//-----------------------------------------------------
module Or8Way (input[7:0] in, output out);
	reg out = 0;
	integer i;

	always @(*) begin
		for (i = 0; i < 8; i = i + 1) begin
			out = out | in[i];
		end
	end
endmodule