// 20-instruction fill.
// Variables:
// Address 0 - current color (0 for white, -1 for black)
// Address 8191 - counter (tracks which row we are drawing to)
// (Note: for the very first iteration of the loop, key input won't be processed. Afterwards, it will respond immediately.)

(DRAW) // Label; runs when we are to draw to the screen.
    // Compute which row to draw to (base screen address + our counter)
    @SCREEN    // Load 16384 (address of VRAM) into A
    D = A      // Store in D (D = 16384)
    @8191      // Load 8191 (counter address) into A
    D = D + M  // Add value at counter address to D.

    // Load the current color to draw with into A, then swap D and A.
    @0         // Load 0 (color address) into A
    A = M + D  // Add value at color address to D, and store in A. (A = color + screen address)
    D = A - D  // Subtract D from A, and store in D. (D = A - screen address = color + screen address - screen address = color).
    A = A - D  // Subtract D from A once more, and store in A (A = A - color = color + screen address - color = screen address).

    // Draw!
    M = D      // Write contents of D (the color) to memory at address 16384 + our counter

    // Increment counter.
    @8191      // Load 8191 (counter address) into A
    D = M + 1  // Add 1 to the counter and store in D.
    M = D & A  // Bitwise AND D with A (8191) and store back in memory at the counter address. 
               // This is equivalent to computing counter % 8192, thus clamping the counter between 0 and 8191 so it doesn't go out of bounds.

(CHECK) // Label; checks the keyboard input and changes the color to draw with if necessary.
    // Fetch the currently pressed key.
    @KBD         // Load 24576 (address for keyboard input) into A
    D = M        // Load value at 24576 (currently presssed key) into D

    // We have two cases to check here:
    // 1. if both the key pressed is 0 (no key pressed), and the current color is 0 (white)
    // 2. if both the key pressed and the current color are not zero (black)
    // In both cases we should jump back to DRAW without changing the current color.
    @0           // Load 0 (color address) into A
    D - M; JEQ   // Subtract the currently pressed key from the color. If they are both 0, this will result in 0.
                 // Jump to address 0 (location of label DRAW) if that is the case.
    D & M; JNE   // Bitwise AND the currently pressed key and the color. If both are not zero, this will result in a non-zero value.
                 // Jump to address 0 (location of label DRAW) if that is the case.

    // Otherwise, swap the colors and reset our counter.
    M = !M       // Bitwise NOT the current color and store it back into memory. This will turn 0 (white) into -1 (black) and vice versa.
    @8191        // Load 8191 (counter address) into A
    AM = 0; JMP  // Set both the counter and A to 0, then unconditionally jump (which takes us to DRAW)