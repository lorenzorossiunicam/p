package it.unicam.pros.pplg.evaluator.purpose.whatifanalysis.util;

import java.util.List;
import java.util.Map;

public class Scenario {

    private String process;
    private Map<String, String> activities;
    private Map<String, List<String>> choices;

    public Scenario(String procID, Map<String, String> acts, Map<String, List<String>> chos){
        this.process = procID;
        this.activities = acts;
        this.choices = chos;
    }

    public String getProcess() {
        return process;
    }

    public Map<String, String> getActivities() {
        return activities;
    }


    public Map<String, List<String>> getChoices() {
        return choices;
    }

    @Override
    public String toString() {
        return "Scenario{" +
                "process='" + process + '\'' +
                ", activities=" + activities +
                ", choices=" + choices +
                '}';
    }
}
