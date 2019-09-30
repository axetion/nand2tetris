//-----------------------------------------------------
// Design Name : Or Testbench
// File Name   : Or_tb.v
// Function    : Testbench for Or gate
//-----------------------------------------------------
module Or_tb;
	reg a, b;
	wire out;

	Or or1 (
		.a (a), .b (b),
		.out (out)
	);

	/* TEST VALUES */
	initial begin
			a = 0; b = 0;
		# 1 a = 0; b = 1;
		# 1 a = 1; b = 0;
		# 1 a = 1; b = 1;

		# 1 $stop;
	end

	initial begin
		$display("| a | b | out |"); 
		$monitor("| %b | %b |  %b  |", a, b, out); 
	end
endmodule