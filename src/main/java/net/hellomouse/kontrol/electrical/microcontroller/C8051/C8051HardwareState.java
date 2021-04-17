package net.hellomouse.kontrol.electrical.microcontroller.C8051;

import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.expressions.storage.Variable;
import net.minecraft.util.Pair;

import java.util.HashMap;

/**
 * The current hardware state of the microcontroller
 * @author Bowserinator
 */
public class C8051HardwareState {
    // TODO: implement all hardware state lmao
    public int P0MDOUT, P1MDOUT, P2MDOUT, P3MDOUT, P74OUT, P3IF, P1MDIN;
    public int P1, P2, P3, P4, P5, P6, P7;
    public int XBRO, XBR1, XBR2;
    public int CKCON, TMOD, TCON, IE, E1E1;
    public int PCA0CN, PCA0MD, PCA0CPM0, PCA0CPM1, PCA0CPM2, PCA0CPM3, PCA0MP4;

    public HashMap<String, Integer> hardwareState = new HashMap<>();
    public HashMap<String, Integer> sbitMap = new HashMap<>();

    public int[] mdoutPorts = new int[4];


    public final boolean[] pinOut = new boolean[32];

    public HashMap<String, Variable> variableMap = new HashMap<>();

    private final C8051Interpreter interpreter;

    public C8051HardwareState(C8051Interpreter interpreter) {
        this.interpreter = interpreter;
    }


    public void updatePorts(C8051Network network) {
        for (int port = 0; port < 4; port++) {
            for (int bit = 0; bit < 8; bit++) {
                int portId = port * 8 + bit;

                // Output enabled bit
                if (((mdoutPorts[port] >> bit) & 1) != 0) {
                    network.setPortVoltage(portId, pinOut[portId] ? 5.0 : 0.0); // TODO: dont hard code
                }
                // Input enabled bit
                else {
                    pinOut[portId] = network.readPortVoltage(portId) > 2.5;
                }
            }
        }

        // TODO: bound check pinId

        // TODO: is input() check for pin ID
        // TODO: cache that

        // TODO: have MUC Core block do this update check
        // MUC block entities?? or have the network update instead
    }

    public void updateVariableState(Variable variable) {
        String typeName = variable.getType().getFullName();


        int portNum = -1;
        int portBit = 0; // -1 = entire port is modified

        if (typeName.equals("sbit")) {      // Single bit (single port)
            Pair<Integer, Integer> portAndBit = C8051PortUtil.getPortAndBitFromAddress(sbitMap.get(variable.getName()));
            portNum = portAndBit.getLeft();
            portBit = portAndBit.getRight();
        }
        else if (typeName.equals("sfr")) {  // Entire port group
            // Update entire port & individual port variables

            String name = variable.getName();

            // TODO: better check + P4-P7
            // https://image3.slideserve.com/6753579/slide31-l.jpg

            if (name.equals("P0")) portNum = 0;
            else if (name.equals("P1")) portNum = 1;
            else if (name.equals("P2")) portNum = 2;
            else if (name.equals("P3")) portNum = 3;

            else if (name.equals("P0MDOUT")) mdoutPorts[0] = ((ConstantValue)variable.getValue()).getBigIntegerValue().intValue();
            else if (name.equals("P1MDOUT")) mdoutPorts[1] = ((ConstantValue)variable.getValue()).getBigIntegerValue().intValue();
            else if (name.equals("P2MDOUT")) mdoutPorts[2] = ((ConstantValue)variable.getValue()).getBigIntegerValue().intValue();
            else if (name.equals("P3MDOUT")) mdoutPorts[3] = ((ConstantValue)variable.getValue()).getBigIntegerValue().intValue();

            portBit = -1;
        }

        if (portNum > -1) {
            int outEnabled = mdoutPorts[portNum]; // TODO bounds check

            if (portBit > -1 && ((outEnabled >> portBit) & 1) != 0) {
                pinOut[portNum * 8 + portBit] = ((ConstantValue)variable.getValue()).getBigIntegerValue().intValue() != 0;
            }
            else {
                int value = ((ConstantValue)variable.getValue()).getBigIntegerValue().intValue();
                for (int i = 0; i < 8; i++) {
                    if (((outEnabled << i) & 1) != 0)
                        pinOut[portNum * 8 + i] = ((value << i) & 1) != 0;
                }
            }
        }
    }

    /**
     * Updates variable values from hardware state if needed
     * @param variable Variable to modify
     */
    public void processVariableAccess(Variable variable) {
        String typeName = variable.getType().getFullName();


        int portNum = -1;
        int portBit = 0; // -1 = entire port is modified

        if (typeName.equals("sbit")) {      // Single bit (single port)
            Pair<Integer, Integer> portAndBit = C8051PortUtil.getPortAndBitFromAddress(sbitMap.get(variable.getName()));
            portNum = portAndBit.getLeft();
            portBit = portAndBit.getRight();
        }

        if (portNum > -1) {
            int outEnabled = mdoutPorts[portNum]; // TODO bounds check

            if (portBit > -1 && ((outEnabled >> portBit) & 1) == 0) {
                variable.setValue(pinOut[portNum * 8 + portBit]  ? ConstantValue.ONE : ConstantValue.ZERO);
            }
            else {
                int value = ((ConstantValue)variable.getValue()).getBigIntegerValue().intValue();
                for (int i = 0; i < 8; i++) {
                    //if (((outEnabled << i) & 1) != 0)
                   //     pinOut[portNum * 8 + i] = ((value << i) & 1) != 0;
                }
            }
        }
    }
}
