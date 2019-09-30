//-----------------------------------------------------
// Design Name : Not16 Testbench
// File Name   : Not16_tb.v
// Function    : Testbench for Not16 gate
//-----------------------------------------------------
module Not16_tb;
	reg[15:0] in;
	wire[15:0] out;

	Not#(16) not1 (
		.in (in),
		.out (out)
	);

	/* TEST VALUES */
	initial begin
			in = 16'b0000_0000_0000_0000;
		# 1 in = 16'b1111_1111_1111_1111;
		# 1 in = 16'b1010_1010_1010_1010;
		# 1 in = 16'b0011_1100_1100_0011;
		# 1 in = 16'b0001_0010_0011_0100;

		# 1 $stop;
	end

	initial begin
		$display("| %16s | %16s |", "in", "out"); 
		$monitor("| %16b | %16b |", in, out); 
	end
endmodule