package net.hellomouse.kontrol.electrical.microcontroller.C8051;

import net.hellomouse.kontrol.electrical.microcontroller.AbstractMUCNetwork;

public class C8051Network extends AbstractMUCNetwork {
    public final C8051Interpreter interpreter;

    public C8051Network(int id) {
        super(id);
        interpreter = new C8051Interpreter();
    }
}
