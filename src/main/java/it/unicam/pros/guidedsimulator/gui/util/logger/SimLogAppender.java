package it.unicam.pros.guidedsimulator.gui.util.logger;

import com.vaadin.flow.component.textfield.TextArea;

public class SimLogAppender {
    private static TextArea area;

    public static void setArea(TextArea target){
        area = target;
        area.setReadOnly(true);
        area.setWidth("100%");
        area.setClassName("console");
    }


    public void close() {
        area.setValue("");
    }


    public static void append(Class c, Level l, String msg) {
        if(area == null){return;}
        area.setValue(area.getValue()+ l.name()+ " --- " +c.getName()+" : "+msg+"\n");
        area.setAutofocus(true);
    }

    public enum Level {
        INFO, WARNING, SEVERE
    }
}
