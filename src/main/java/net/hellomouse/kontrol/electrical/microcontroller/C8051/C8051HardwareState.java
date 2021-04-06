package net.hellomouse.kontrol.electrical.microcontroller.C8051;

import java.util.HashMap;

public class C8051HardwareState {
    // TODO: implement all hardware state lmao
    public int P0MDOUT, P1MDOUT, P2MDOUT, P3MDOUT, P74OUT, P3IF, P1MDIN;
    public int P1, P2, P3, P4, P5, P6, P7;
    public int XBRO, XBR1, XBR2;
    public int CKCON, TMOD, TCON, IE, E1E1;
    public int PCA0CN, PCA0MD, PCA0CPM0, PCA0CPM1, PCA0CPM2, PCA0CPM3, PCA0MP4;

    public HashMap<String, Integer> hardwareState = new HashMap<>();

    public HashMap<String, Integer> sbitMap = new HashMap<>();
}
