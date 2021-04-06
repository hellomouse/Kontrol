package net.hellomouse.c_interp.compiler;

import net.hellomouse.c_interp.instructions.AbstractInstruction;

import java.util.ArrayList;

/**
 * Handles instruction adding during compile time
 * @author Bowserinator
 */
public class CompilerInstructions {
    private ArrayList<AbstractInstruction> instructions = new ArrayList<>();

    /** Construct a compiler instruction handler */
    public CompilerInstructions() {}

    /** Sequentially getStringValue all instructions */
    public void printInstructions() {
        for (AbstractInstruction instr : instructions)
            System.out.println(instr.toASMLine());
    }

    /** Add a new instruction */
    public void addInstruction(AbstractInstruction instr) { instructions.add(instr); }

    /** Get reference to all instructions */
    public ArrayList<AbstractInstruction> getInstructions() { return instructions; }

    /** Get address of current instruction (1st instr = 0, 2nd = 1, etc...) */
    public int currentInstructionAddress() { return instructions.size() - 1; }

    /** Clear the instructions array */
    public void clear() { instructions = new ArrayList<>(); }
}
