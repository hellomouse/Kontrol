package net.hellomouse.c_interp.common;

public interface ISavable {
    byte[] save();
    Object load(byte[] data);
}
