package net.hellomouse.c_interp.interpreter;


import java.util.Date;
import java.util.logging.*;

public class InterpreterLogger {

    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_RESET = "\033[0m";

    // assumes the current class is called MyLogger
    public static final Logger LOGGER = Logger.getLogger("Interpreter");
    static {
        LOGGER.setLevel(Level.ALL);

        Handler handler = new ConsoleHandler();
        // handler.setLevel(Level.ALL);
        handler.setLevel(Level.OFF);

        handler.setFormatter(new SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                String newFormat = format;
                if (Level.INFO.equals(lr.getLevel()))
                    newFormat = ANSI_BLUE + newFormat;
                else if (Level.FINE.equals(lr.getLevel()))
                    newFormat = ANSI_CYAN + newFormat;
                else if (Level.FINER.equals(lr.getLevel()))
                    newFormat = ANSI_GREEN + newFormat;
                else if (Level.WARNING.equals(lr.getLevel()))
                    newFormat = ANSI_YELLOW + newFormat;
                else if (Level.SEVERE.equals(lr.getLevel()))
                    newFormat = ANSI_RED + newFormat;
                else
                    newFormat = ANSI_WHITE + newFormat;

                newFormat += ANSI_RESET;

                return String.format(newFormat,
                        new Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage()
                );
            }
        });
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);
    }

    public static final String INDENT = "    ";

    private final InterpreterState state;

    public InterpreterLogger(InterpreterState state) {
        this.state = state;
    }

    public void entryExitPoint(String msg) {
        info(ANSI_GREEN + msg);
    }

    public void definition(String msg) {
        info(ANSI_PURPLE + msg);
    }

    public void background(String msg) {
        fine(ANSI_WHITE + msg);
    }

    public void controlFlow(String msg) {
        info(ANSI_CYAN + msg);
    }

    public void register(String msg) {
        fine(ANSI_RED + " " + msg);
    }

    public void finest(String msg) {
        LOGGER.finest(wrapString(msg, state.scopes.size()));
    }

    public void finer(String msg) {
        LOGGER.finer(wrapString(msg, state.scopes.size()));
    }

    public void fine(String msg) {
        LOGGER.fine(wrapString(msg, state.scopes.size()));
    }

    public void info(String msg) {
        LOGGER.info(wrapString(msg, state.scopes.size()));
    }

    public void warning(String msg) {
        LOGGER.warning(wrapString(msg, state.scopes.size()));
    }

    public void severe(String msg) {
        LOGGER.severe(wrapString(msg, state.scopes.size()));
    }

    public String wrapString(String input, int indentLevel) {
        return INDENT.repeat(indentLevel) + input;
    }
}


