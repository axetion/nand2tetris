# nand2tetris

My assembler, IL translator and standard library implementations for the nand2tetris curriculum (as of fall 2018 when I wrote all of this), as well as a few of the more interesting chips written in Nand2Tetris HDL, an absurdly over-optimized "fill the screen" assembly program for testing, and Verilog implementations of the basic logic chips as a bonus exercise.

The assembler has nothing particularly special to it other than some helpful error checking and linting.
The IL translator tries its best to optimize stack operations, typically resulting in about a 50% reduction over the size of the outputs from other implementations I've seen. It's rather stupid and greedy, however.
The standard library has a few interesting micro-optimizations to it, mostly in Math and Screen.

(If the assembly examples seem overcommented, it's because my instructor wanted it as part of the grading rubric.)
