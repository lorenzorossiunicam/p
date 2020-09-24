package it.unicam.pros.purple.gui.ui.view.whatif;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import java.util.Map;
import java.util.Set;

public class XorPercentageValidator implements Validator<Double> {

    private NumberField nF;
    private Set<NumberField> correlates;

    public XorPercentageValidator(NumberField f, Map<NumberField, Set<NumberField>> cc) {
        this.nF = f;
        this.correlates = cc.get(nF);
    }

    @Override
    public ValidationResult apply(Double obj, ValueContext valueContext) {
        double sum = obj;
        for (NumberField o : correlates){
            sum += o.getValue();
        }
        if(sum == 100) return ValidationResult.ok();
        return ValidationResult.error("The sum must be 100.");
    }
}
