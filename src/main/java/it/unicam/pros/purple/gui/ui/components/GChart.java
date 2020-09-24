package it.unicam.pros.purple.gui.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.JsModule;

import java.util.Map;

@JsModule("./gchart.js")
public class GChart {

    private Html component;
    public GChart(String x, String y, Map<String,Double> data){
        String cData = "";
        for (String d : data.keySet()){
            cData += ", [\""+d+"\", "+data.get(d)+"]";
        }
        component = new Html(" <google-chart data='[[\""+x+"\", \""+y+"\"] "+cData+"]'></google-chart>");
    }

    public Component getComponent(){
        return component;
    }
}
