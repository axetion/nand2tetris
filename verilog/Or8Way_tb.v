//-----------------------------------------------------
// Design Name : Or8Way Testbench
// File Name   : Or8Way_tb.v
// Function    : Testbench for Or8Way gate
//-----------------------------------------------------
module Or8Way_tb;
	reg[7:0] in;
	wire out;

	Or8Way or1 (
		.in (in),
		.out (out)
	);

	/* TEST VALUES */
	initial begin
			in = 8'b0000_0000;
		# 1 in = 8'b1111_1111;
		# 1 in = 16'b0001_0000;
		# 1 in = 16'b0000_0001;
		# 1 in = 16'b0010_0110;

		# 1 $stop;
	end

	initial begin
		$display("|    in    | out |"); 
		$monitor("| %8b |  %b  |", in, out); 
	end
endmodule