package it.unicam.pros.guidedsimulator.evaluator.purpose.rediscoverability;


import it.unicam.pros.guidedsimulator.evaluator.Evaluator;

public abstract class Rediscoverability implements Evaluator {

    public enum RediscoverabilityAlgo{
        ALPHA, ALPHA_PLUS, ALPHA_PLUS_PLUS, ALPA_SHARP, ALPHA_DOLLAR
    }
}
