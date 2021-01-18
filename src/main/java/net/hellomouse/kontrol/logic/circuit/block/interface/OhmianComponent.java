package net.hellomouse.kontrol.logic.circuit.components;

public interface OhmianComponent {
    public static final int BLOCK_TO_PX = 16; // 16px = 1 block

    /**
     * Returns resistivity / px, 16px = 1 block
     * Used to compute as some nodes are sub-block
     * @return Resistivity / px
     */
    public float getResistivity();
}
