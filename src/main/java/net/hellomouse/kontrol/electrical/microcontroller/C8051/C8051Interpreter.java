package net.hellomouse.kontrol.electrical.microcontroller.C8051;

import net.hellomouse.c_interp.antlr4.CLexer;
import net.hellomouse.c_interp.antlr4.CParser;
import net.hellomouse.c_interp.common.Machine;
import net.hellomouse.c_interp.common.MachineSettings;
import net.hellomouse.c_interp.common.expressions.interfaces.IRuntimeConstant;
import net.hellomouse.c_interp.common.expressions.storage.ConstantValue;
import net.hellomouse.c_interp.common.storage_types.base.IntBaseTypeStorage;
import net.hellomouse.c_interp.compiler.Compiler;
import net.hellomouse.c_interp.interpreter.Interpreter;
import net.hellomouse.c_interp.interpreter.InterpreterListenerHandler;
import net.hellomouse.c_interp.interpreter.InterpreterSettings;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.antlr.v4.runtime.CharStreams.fromFileName;

public class C8051Interpreter {
    // TODO Construct w/ code
    // have variable modifications in this
    // so if change only need to change that

    private final Interpreter interpreter;

    public final C8051HardwareState state = new C8051HardwareState();

    // • Up to 8 kB of on-chip Flash memory—512 bytes are reserved
    //• 768 bytes of on-chip RAM
    private static final Machine machine = new Machine(new MachineSettings()
        .charIsSigned(true)
        .integerTypeSizes(1, 2, 2, 4, 4)
        .memorySize(8192000, 2));

    public C8051Interpreter() {
        interpreter = new Interpreter(machine, new InterpreterSettings());
        interpreter.listeners.attach(new InterpreterListenerHandler.InterpreterListener()
            .onVariableDeclaration(variable -> {
                // Check for special variables directly linked to hardware state
                String typeName = variable.getType().getFullName();

                if (typeName.equals("sbit")) {      // Single bit (single port)
                    System.out.println("Hello world " + variable.getValue() + " " + variable.getName());

                    state.sbitMap.put(variable.getName(), ((ConstantValue)variable.getValue().runtimeEval(interpreter.getState())).getBigIntegerValue().intValue());
                }
                else if (typeName.equals("sfr")) {  // Entire port group
                    state.hardwareState.put(variable.getName(), 0);
                }

                return null;
            })
            .onVariableChange(variable -> {
                System.out.println("VARIABLE " + variable.getName());

                int sbit = state.sbitMap.getOrDefault(variable.getName(), -1);
                if (sbit >= 0) {
                    // Change connected port map
                    // todo: state port ID => is it on??
                    // state.updatePort func
                }


                String typeName = variable.getType().getFullName();

                if (typeName.equals("sbit")) {      // Single bit (single port)

                }
                else if (typeName.equals("sfr")) {  // Entire port group
                    state.hardwareState.put(variable.getName(), 0);
                }

                // TODO: also SFR
                // TODO: hardware state should be a hashmap

                if (variable.getName().equals("P0MDOUT"))
                    System.out.println(variable.getName() + " = " + variable.getValue());
                return null;
            })
        );

        Compiler compiler = new Compiler(machine);
        compiler.machine.primitives.injectPrimitive(new IntBaseTypeStorage("sfr", 1, 1));
        compiler.machine.primitives.injectPrimitive(new IntBaseTypeStorage("sbit", 1, 1));
        compiler.machine.primitives.injectPrimitive(new IntBaseTypeStorage("uint8", 4, 2));

        loadFile("D:\\Kontrol\\src\\main\\java\\net\\hellomouse\\kontrol\\electrical\\microcontroller\\C8051/headers/c8051f020.h", compiler);
        loadFile("D:\\Kontrol\\src\\main\\java\\net\\hellomouse\\kontrol\\electrical\\microcontroller\\C8051/headers/c8051_SDCC.h", compiler);
        loadFile("D:\\Kontrol\\src\\main\\java\\net\\hellomouse\\kontrol\\electrical\\microcontroller\\C8051/temporary/sample.c", compiler);

        interpreter.injectFunction("printf", ars -> {
            ArrayList<String> params = new ArrayList<>();
            for (int j = 1; j < ars.length;j++)
                params.add(((IRuntimeConstant)ars[j].getValue()).getStringValue());

            System.out.printf( ((IRuntimeConstant)ars[0].getValue()).getStringValue(), params.toArray());
            return null;
        });
        interpreter.setInstructions(compiler.instructions.getInstructions());
        interpreter.interpret(1000000000);

        interpreter.callFunction("main");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            interpreter.interpret(10);
        }, 0, 100, TimeUnit.MICROSECONDS);
    }

    private void loadFile(String filename, Compiler compiler) {
        try {
            CharStream cs = fromFileName(filename);
            CLexer lexer = new CLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CParser parser = new CParser(tokens);

            compiler.compile(parser, cs);
        }
        catch(IOException e) {
            throw new IllegalStateException("IO exception caught: " + e.getMessage());
        }
    }
}
