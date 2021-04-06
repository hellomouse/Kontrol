package net.hellomouse.c_interp.common.scope;

import net.hellomouse.c_interp.common.expressions.labels.NamedLabel;
import net.hellomouse.c_interp.common.expressions.labels.SwitchCaseLabel;
import net.hellomouse.c_interp.common.storage_types.FunctionTypeStorage;
import net.hellomouse.c_interp.common.storage_types.StructOrUnionStorage;
import net.hellomouse.c_interp.common.storage_types.interfaces.INamedType;
import net.hellomouse.c_interp.instructions.statement.JumpInstructions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A local scope, contains various local definitions and declarations
 * @author Bowserinator
 */
public class LocalScope extends AbstractScope {
    private AbstractScope loopScope = null;
    private AbstractScope switchScope = null;
    private LocalScope functionScope = null;

    public final ArrayList<JumpInstructions.ContinueInstruction> continueInstructions = new ArrayList<>();
    public final ArrayList<JumpInstructions.BreakInstruction> breakInstructions = new ArrayList<>();

    public final ArrayList<SwitchCaseLabel> switchLabels = new ArrayList<>();
    public final HashSet<String> switchLabelNames = new HashSet<>();

    public final HashMap<String, NamedLabel> labels = new HashMap<>();
    public final ArrayList<JumpInstructions.GotoInstruction> gotoInstructions = new ArrayList<>();

    private int continueAddress = -1;
    private int breakAddress = -1;

    public GlobalScope global;  // Reference to global scope

    /**
     * Construct an local scope
     * @param parent Parent scope
     */
    public LocalScope(AbstractScope parent, String id) {
        super(parent, id);

        if (parent instanceof GlobalScope) {
            this.global = (GlobalScope) parent;
            this.functionScope = this;
        }
        else if (parent instanceof LocalScope) {
            this.global = ((LocalScope)parent).global;
            this.continueAddress = ((LocalScope)parent).continueAddress;
            this.breakAddress = ((LocalScope)parent).breakAddress;

            this.functionScope = ((LocalScope)parent).getFunctionScope();
            this.loopScope = ((LocalScope)parent).getLoopScope();
            this.switchScope = ((LocalScope)parent).getSwitchScope();
        }
    }

    /**
     * Add a new struct or union type to the scope. Type should not
     * be an incomplete type
     * @param type StructOrUnionStorage
     */
    @Override
    public void addStructOrUnionType(StructOrUnionStorage type) {
        String name = ((INamedType)type).getName();
        customStructOrUnionTypes.put(name, type);
    }

    @Override
    public FunctionTypeStorage getFunction(String name) {
        // Call can be changed in the future for local function definitions
        return global.getFunction(name);
    }

    public boolean isInLoop() { return loopScope != null; }
    public boolean isInSwitch() { return switchScope != null; }

    public void markAsLoop() {
        this.loopScope = this;
        this.switchScope = null;
    }

    public void markAsSwitch() {
        this.switchScope = this;
        this.loopScope = null;
    }

    public AbstractScope getLoopScope() { return loopScope; }
    public AbstractScope getSwitchScope() { return switchScope; }
    public LocalScope getFunctionScope() { return functionScope; }

    public AbstractScope getLoopOrSwitchScope() {
        if (loopScope != null)
            return loopScope;
        return switchScope;
    }

    public void setContinueAddress(int address) {
        if (this.loopScope == null)
            return;

        this.continueAddress = address;
        for (JumpInstructions.ContinueInstruction continueInstruction : continueInstructions) {
            continueInstruction.setJumpAddress(address);
        }
    }

    public void setBreakAddress(int address) {
        if (this.loopScope == null && this.switchScope == null)
            return;

        this.breakAddress = address;
        for (JumpInstructions.BreakInstruction breakInstruction : breakInstructions) {
            breakInstruction.setJumpAddress(address);
        }
    }

    public void addSwitchLabel(SwitchCaseLabel label) {
        switchLabels.add(label);
        switchLabelNames.add(label.value == null ? "default" : label.value.toString());
    }

    public void postProcess() {
        for (JumpInstructions.GotoInstruction gotoInstruction : gotoInstructions) {
            gotoInstruction.setJumpAddress(labels.get(gotoInstruction.label).address);
        }
    }

    @Override
    public String getId() {
        if (parent == null || parent.getId().length() == 0)
            return id;
        return parent.getId() + "." + id + "-" + parent.getChildCount();
    }
}
