package net.hellomouse.kontrol.logic.circuit.virtual;

import net.hellomouse.kontrol.logic.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.logic.circuit.virtual.components.*;

public class LinearCircuitTest {
    public static void main(String[] args) {
        VirtualCircuit circuit = new VirtualCircuit();

//        circuit.addComponent(new VirtualResistor( 1), 0, 1);
//        circuit.addComponent(new VirtualResistor( 2), 1, 2);
//        circuit.addComponent(new VirtualResistor( 3), 2, 3);
//        circuit.addComponent(new VirtualVoltageSource(10), 3, 4);
//        circuit.addComponent(new VirtualResistor( 3), 4, 5);
//        circuit.addComponent(new VirtualVoltageSource(10), 5, 0);
//        circuit.addComponent(new VirtualGround(), 0, 0);

//        VirtualResistor R1 = new VirtualResistor( 1);
//        VirtualResistor R2 = new VirtualResistor( 2);
//
//        circuit.addComponent(R1, 0, 1);
//        circuit.addComponent(new VirtualVoltageSource(10), 1, 2);
//        circuit.addComponent(R2, 2, 0);
//        circuit.addComponent(new VirtualGround(), 0, 0);

//        VirtualCapacitor C1 = new VirtualCapacitor(0.001);
//        VirtualResistor R1 = new VirtualResistor(10);
//        VirtualResistor R2 = new VirtualResistor(10);
//        circuit.addComponent(R1, 0, 1);
//        circuit.addComponent(new VirtualVoltageSource(10), 1, 2);
//        circuit.addComponent(R2, 2, 3);
//        circuit.addComponent(C1, 3, 0);
//        circuit.addComponent(new VirtualGround(), 2, 2);
//
//        circuit.solve();
//        System.out.println("");
//        System.out.println(R1.getVoltage());
//        System.out.println(R2.getVoltage());
//        System.out.println(C1.getVoltage());
//        System.out.println("");
//
//        for (int i = 0; i < 10; i++) {
//            circuit.tick();
//            circuit.solve();
//        }
//
//        System.out.println("");
//        circuit.solve();
//        System.out.println("");
//        System.out.println(R1.getVoltage());
//        System.out.println(R2.getVoltage());
//        System.out.println(C1.getVoltage());
//        System.out.println(R1.getCurrent());

        VirtualDiode D1 = new VirtualDiode(0.7);
        circuit.addComponent(new VirtualVoltageSource(-10), 0, 1);
        // circuit.addComponent(new VirtualVoltageSource(10), 2, 3);
        circuit.addComponent(new VirtualResistor( 1), 1, 2);
        circuit.addComponent(D1, 2, 3);
        circuit.addComponent(new VirtualResistor( 1), 3, 0);
        circuit.addComponent(new VirtualGround(), 0, 0);
        circuit.solve();
        System.out.println(D1.getCurrent());
    }
}
