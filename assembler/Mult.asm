// 10-instruction multiplication.
// Variables:
// Address 0 - multiplicand
// Address 1 - multiplier
// Address 2 - result (multiplicand * multiplier)
// (Note: clobbers argument 1. The test script allows this.)

// Zero result location.
    @2     // Load 2 into A
    M = 0  // Set value at address 2 (result) to 0

(LOOP) // Label; beginning of our multiplication loop
    // Decrement multiplier.
    @1          // Load 1 into A
    MD = M - 1  // Decrement value at address 1 (multiplier), and also load into D
    @END        // Load address for label END
    D; JLT      // If D is less than 0, jump to end

    // Add multiplicand to result.
    @0               // Load 0 into A
    D = M            // Load value at address 0 into D (multiplicand)
    @2               // Load 2 into A
    M = M + D; JNE   // Add D to value at address 2 (result).
                     // If it's not 0, jump to PC position 2... which happens to be (LOOP).
                     // Jumping when not 0 has the additional advantage of ending early if the multiplicand is 0, since M will be 0
(END) // Label; we jump here at the end. I tried this program with the infinite loop the sample programs use, and the emulator didn't terminate
      // the program any earlier so I omitted it.