package net.hellomouse.c_interp.interpreter;

public class InterpreterSettings {
    private long stackSize = -1;
    private long heapSize = -1;
    private long clockSpeed = -1; // In instructions / tick
    private long recursionLimit = -1;

    private boolean locked = false;

    public InterpreterSettings() {}

    public InterpreterSettings stackSize(long stackSize) {
        checkLock();
        this.stackSize = stackSize;
        return this;
    }

    public InterpreterSettings heapSize(long heapSize) {
        checkLock();
        this.heapSize = heapSize;
        return this;
    }

    public InterpreterSettings clockSpeed(long clockSpeed) {
        checkLock();
        this.clockSpeed = clockSpeed;
        return this;
    }

    public InterpreterSettings recursionLimit(long recursionLimit) {
        checkLock();
        this.recursionLimit = recursionLimit;
        return this;
    }


    public void lock() {
        if (stackSize <= 0 || heapSize <= 0)
            throw new IllegalStateException("Invalid stack or heap sizes provided (" + stackSize + "/" + heapSize + ")");
        if (clockSpeed <= 0)
            throw new IllegalStateException("Clock speed of " + clockSpeed + " is invalid");
        locked = true;
    }

    private void checkLock() {
        if (locked)
            throw new IllegalStateException("InterpreterSettings instance is locked, cannot be changed");
    }
}
