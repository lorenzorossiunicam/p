package it.unicam.pros.guidedsimulator.gui.ui.view.whatif;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.unicam.pros.guidedsimulator.evaluator.purpose.whatifanalysis.util.Scenario;
import it.unicam.pros.guidedsimulator.evaluator.purpose.whatifanalysis.util.ScenariosParamsExtractor;
import it.unicam.pros.guidedsimulator.gui.ui.components.FlexBoxLayout;
import it.unicam.pros.guidedsimulator.gui.ui.components.GChart;
import it.unicam.pros.guidedsimulator.gui.ui.layout.size.Bottom;
import it.unicam.pros.guidedsimulator.gui.ui.layout.size.Horizontal;
import it.unicam.pros.guidedsimulator.gui.ui.layout.size.Right;
import it.unicam.pros.guidedsimulator.gui.ui.layout.size.Top;
import it.unicam.pros.guidedsimulator.gui.ui.util.IconSize;
import it.unicam.pros.guidedsimulator.gui.ui.util.LumoStyles;
import it.unicam.pros.guidedsimulator.gui.ui.util.TextColor;
import it.unicam.pros.guidedsimulator.gui.ui.util.UIUtils;
import it.unicam.pros.guidedsimulator.gui.ui.util.css.*;
import it.unicam.pros.guidedsimulator.gui.ui.view.MainLayout;
import it.unicam.pros.guidedsimulator.gui.ui.view.ViewFrame;
import it.unicam.pros.guidedsimulator.gui.util.Uploader;
import it.unicam.pros.guidedsimulator.guidedsimulator.GuidedSimulator;
import it.unicam.pros.guidedsimulator.util.eventlogs.EventLog;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.Trace;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;
import it.unicam.pros.guidedsimulator.util.eventlogs.utils.LogIO;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayOutputStream;
import java.util.*;


@Route(value="whatif/bpmn", layout = MainLayout.class)
@PageTitle("BPMN What If Analysis | GuidedSimulator")
public class BPMNWhatIfAnalysisView extends ViewFrame {

    public static final String MAX_WIDTH = "1024px";
    private ByteArrayOutputStream logStream;
    private Map<String, Scenario> o;
    private Map<String, NumberField> activities = new HashMap<String, NumberField>();
    private Map<String, Map<String, NumberField>> choices = new HashMap<String, Map<String, NumberField>>();
    private Map<NumberField, Set<NumberField>> correlateChoices = new HashMap<NumberField, Set<NumberField>>();
    private BpmnModelInstance mi;
    private Button genBtn;
    private Uploader upl;
    private NumberField tauField;
    private IntegerField maxTraces;
    private Button downloadBtn;
    private VerticalLayout form;
    private Select<String> currency;
    private EventLog log;
    private FlexBoxLayout statistics;
    private Row stats;

    public BPMNWhatIfAnalysisView(){

        form = new VerticalLayout();
        genBtn = new Button("Generate Log");
        upl = new Uploader("application/octet-stream",".bpmn");
        tauField = new NumberField("% of Precision");
        maxTraces = new IntegerField("Max trace number");
        downloadBtn = new Button("Download Log");
        setViewContent(createContent());
    }

    private Component createContent() {
        Component inputs = createInputs();
        Component actions = createActions();
        Component form = createForm();
        Component stats = createStats();
        stats.setVisible(false);

        FlexBoxLayout content = new FlexBoxLayout(inputs, actions,stats, form);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setFlexDirection(FlexDirection.COLUMN);
        return content;
    }

    private FlexBoxLayout createHeader(VaadinIcon icon, String title) {
        FlexBoxLayout header = new FlexBoxLayout(
                UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, icon),
                UIUtils.createH3Label(title));
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setMargin(Bottom.L, Horizontal.RESPONSIVE_L);
        header.setSpacing(Right.L);
        return header;
    }

    private Component createInputs() {
        FlexBoxLayout inputs = new FlexBoxLayout(
                createHeader(VaadinIcon.INPUT, "Inputs"),
                createInputsComponents());
        inputs.setBoxSizing(BoxSizing.BORDER_BOX);
        inputs.setDisplay(Display.BLOCK);
        inputs.setMargin(Top.L);
        inputs.setMaxWidth(MAX_WIDTH);
        inputs.setPadding(Horizontal.RESPONSIVE_L);
        inputs.setWidthFull();
        return inputs;
    }

    private Component createInputsComponents() {
        Row inputs = new Row();
        UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, inputs);
        UIUtils.setBorderRadius(BorderRadius.S, inputs);
        UIUtils.setShadow(Shadow.XS, inputs);

        //Uploader
        upl.getUploadComponent().addSucceededListener(event -> {
            refactor();
            mi = Bpmn.readModelFromStream(upl.getStream());
            o  = ScenariosParamsExtractor.extractParams(mi);
            populateForm(form, o);
            genBtn.setEnabled(true);
        });

        //Tau field
        tauField.setValue(100d);
        tauField.setHasControls(true);
        tauField.setMin(0);
        tauField.setMax(100);

        maxTraces.setValue(10000);
        maxTraces.setMin(1);

        inputs.add(upl.getUploadComponent(), tauField, maxTraces);
        return inputs;
    }

    private Component createActions() {
        FlexBoxLayout actions = new FlexBoxLayout(
                createHeader(VaadinIcon.HAND, "Actions"),
                createActionsComponents());
        actions.setBoxSizing(BoxSizing.BORDER_BOX);
        actions.setDisplay(Display.BLOCK);
        actions.setMargin(Top.L);
        actions.setMaxWidth(MAX_WIDTH);
        actions.setPadding(Horizontal.RESPONSIVE_L);
        actions.setWidthFull();
        return actions;
    }

    private Component createActionsComponents() {
        Row actions = new Row();
        UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, actions);
        UIUtils.setBorderRadius(BorderRadius.S, actions);
        UIUtils.setShadow(Shadow.XS, actions);

        //Log generation button
        genBtn.setEnabled(false);
        genBtn.addClickListener(event -> {
            log = GuidedSimulator.whatif(mi, getChoicesParams(), getActivityCosts(),tauField.getValue(), maxTraces.getValue());
            logStream = LogIO.getAsStream(log);
            populateStatistics();
            statistics.setVisible(true);
            downloadBtn.setEnabled(true);
        });

        //Download button
        downloadBtn.setEnabled(false);
        downloadBtn.setWidth("100%");
        FileDownloadWrapper link = new FileDownloadWrapper("log.xes", () -> {
            return logStream.toByteArray();
        });
        link.wrapComponent(downloadBtn);
        link.setWidth("100%");

        actions.add(genBtn, link);
        return actions;
    }



    private Map<String, Double> getActivityCosts() {
        Map<String, Double> ret = new HashMap<String, Double>();
        for (String t : activities.keySet()){
            ret.put(t, activities.get(t).getValue());
        }
        return ret;
    }

    private Component createForm() {
        FlexBoxLayout actions = new FlexBoxLayout(
                createHeader(VaadinIcon.HAND, "Options"),
                createFormComponents());
        actions.setBoxSizing(BoxSizing.BORDER_BOX);
        actions.setDisplay(Display.BLOCK);
        actions.setMargin(Top.L);
        actions.setMaxWidth(MAX_WIDTH);
        actions.setPadding(Horizontal.RESPONSIVE_L);
        actions.setWidthFull();
        return actions;
    }

    private Component createFormComponents() {
        Row params = new Row();
        UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, params);
        UIUtils.setBorderRadius(BorderRadius.S, params);
        UIUtils.setShadow(Shadow.XS, params);

        params.add(form);
        return params;
    }

    private Component createStats() {
        stats = new Row();
        UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, stats);
        UIUtils.setBorderRadius(BorderRadius.S, stats);
        UIUtils.setShadow(Shadow.XS, stats);

        statistics = new FlexBoxLayout(
                createHeader(VaadinIcon.CHART, "Statistics"),
                stats);
        statistics.setBoxSizing(BoxSizing.BORDER_BOX);
        statistics.setDisplay(Display.BLOCK);
        statistics.setMargin(Top.L);
        statistics.setMaxWidth(MAX_WIDTH);
        statistics.setPadding(Horizontal.RESPONSIVE_L);
        statistics.setWidthFull();
        return statistics;
    }


    private void populateStatistics() {
        stats.removeAll();
        Double min = null, max = 0.0, avg = 0.0;
        int nTraces = log.size();
        for(Trace t : log.getTraces()){
            double tCost = 0.0;
            for (Event e : t.getTrace()){
                if (e.getCost()==null) continue;
                tCost += e.getCost();
            }
            if (min == null || tCost < min){
                min = tCost;
            }
            if (tCost > max){
                max = tCost;
            }
            avg += tCost;
        }
        avg = avg / nTraces;
        H4 results = new H4("Min: "+min+" Max: "+max+" Average: "+avg);
        Map<String, Double> d = new HashMap<String, Double>(3);
        d.put("Min", min);
        d.put("Max", max);
        d.put("Avg", avg);
        GChart chart = new GChart("", "Cost", d);
        stats.add(new VerticalLayout(new H3("#Traces"),new H4(String.valueOf(nTraces)),new H3("Costs"),results, chart.getComponent()));

    }

    private  Map<String, Double> getChoicesParams() {
        Map<String, Double> ret = new HashMap<String, Double>();
        for (Map<String, NumberField> sfProb : choices.values()){
            for (String sf : sfProb.keySet()){
                ret.put(sf, sfProb.get(sf).getValue());
            }
        }
        return ret;
    }



    private void refactor() {
        form.removeAll();
        stats.removeAll();
    }


    private Component populateForm(VerticalLayout layout, Map<String, Scenario> content) {
        layout.removeAll();
//        layout.add(new HorizontalLayout(new H3("Currency:"),currency));
        for (String pId : content.keySet()){
            Scenario proc = content.get(pId);
            layout.add(new HorizontalLayout(new H3("Process:"),new H3(proc.getProcess())));
            Map<String,String> acts = proc.getActivities();
            layout.add(new H4("Activities:"));
            for(String tId : acts.keySet()){
                NumberField tmp = new NumberField();
                tmp.setMin(0);
                tmp.setValue(1.0);
                activities.put(tId, tmp);
                layout.add(new HorizontalLayout(new H5(acts.get(tId)), tmp));
            }
            Map<String, List<String>> gatws = proc.getChoices();
            layout.add(new H4("Choices:"));
            for(String gId : gatws.keySet()){
                layout.add(new Text(gId));
                List<String> sFlows = gatws.get(gId);
                choices.put(gId, new HashMap<String, NumberField>());
                Set<NumberField> tmp = new HashSet<NumberField>();
                for(String sFlow : sFlows){
                    NumberField f = new NumberField();
                    f.setValue(100.0/sFlows.size());
                    tmp.add(f);
                    f.addValueChangeListener(event -> {
                        checkConform(event.getSource());
                    });
                    choices.get(gId).put(sFlow, f);
                    layout.add(new HorizontalLayout(new H5(sFlow), f, new H5("%")));
                }
                for(NumberField f : tmp){
                    correlateChoices.put(f, new HashSet<NumberField>());
                    for (NumberField f1 : tmp){
                        if (f.equals(f1)){continue;}
                        correlateChoices.get(f).add(f1);
                    }
                }
            }
        }
        return layout;
    }

    private void checkConform(NumberField source) {
        Set<NumberField> others = correlateChoices.get(source);
        double sum = 0;
        for (NumberField o : others){
            sum += o.getValue();
        }
        if (source.getValue() + sum > 100) {
            source.setValue(100-sum);
        } 
//        if (source.getValue() + sum < 100){
//            for (NumberField o : others){
//                o.set
//            }
//        }
    }
}
