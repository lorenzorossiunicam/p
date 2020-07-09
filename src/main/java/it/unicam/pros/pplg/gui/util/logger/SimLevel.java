package it.unicam.pros.pplg.gui.util.logger;

import java.util.logging.Level;

public class SimLevel extends Level {
    public static final Level SIM_INFO = new SimLevel("SIM_INFO", Level.INFO.intValue() + 1);

    public SimLevel(String name, int value) {
        super(name, value);
    }
}

