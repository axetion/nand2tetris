//-----------------------------------------------------
// Design Name : DMux Testbench
// File Name   : DMux_tb.v
// Function    : Testbench for DMux gate
//-----------------------------------------------------
module DMux_tb;
	reg in, sel;
	wire a, b;

	DMux dmux1 (
		.in (in), .sel (sel),
		.a (a), .b (b)
	);

	/* TEST VALUES */
	initial begin
			in = 0; sel = 0;
		# 1 in = 0; sel = 1;
		# 1 in = 1; sel = 0;
		# 1 in = 1; sel = 1;

		# 1 $stop;
	end

	initial begin
		$display("| in | sel | a | b |"); 
		$monitor("|  %b |  %b  | %b | %b |", in, sel, a, b); 
	end
endmodule