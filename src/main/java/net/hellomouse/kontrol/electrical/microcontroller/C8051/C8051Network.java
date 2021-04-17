package net.hellomouse.kontrol.electrical.microcontroller.C8051;

import net.hellomouse.kontrol.electrical.microcontroller.AbstractMUCNetwork;
import net.minecraft.util.math.Direction;

public class C8051Network extends AbstractMUCNetwork {
    public final C8051Interpreter interpreter;

    public C8051Network(int id) {
        super(id);
        interpreter = new C8051Interpreter();
    }

    @Override
    public void tick() {
        interpreter.state.updatePorts(this);
        interpreter.interpreter.interpret(1);
    }

    @Override
    public boolean coreCanFloodfillDir(Direction direction) {
        return direction != Direction.UP && direction != Direction.DOWN;
    }

    @Override
    public int maxPorts() { return 256; }
}
