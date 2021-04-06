package net.hellomouse.c_interp.common.storage_types;

import net.hellomouse.c_interp.common.MachineSettings;

public class InvalidTypeStorage extends AbstractTypeStorage {
    public InvalidTypeStorage() {};

    @Override
    public String getFullName() { return "invalid"; }

    @Override
    public int getSize() { return MachineSettings.INVALID_SIZE; }
}
