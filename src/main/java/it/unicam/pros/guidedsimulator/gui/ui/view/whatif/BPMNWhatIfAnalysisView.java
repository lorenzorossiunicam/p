package it.unicam.pros.guidedsimulator.gui.ui.view.whatif;


import com.awesomecontrols.quickpopup.QuickPopup;
import com.vaadin.annotations.Push;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.*;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.shared.communication.PushMode;
import it.unicam.pros.guidedsimulator.evaluator.purpose.whatifanalysis.util.Scenario;
import it.unicam.pros.guidedsimulator.evaluator.purpose.whatifanalysis.util.ScenariosParamsExtractor;
import it.unicam.pros.guidedsimulator.gui.ui.components.FlexBoxLayout;
import it.unicam.pros.guidedsimulator.gui.ui.components.GChart;
import it.unicam.pros.guidedsimulator.gui.ui.layout.size.*;
import it.unicam.pros.guidedsimulator.gui.ui.util.IconSize;
import it.unicam.pros.guidedsimulator.gui.ui.util.LumoStyles;
import it.unicam.pros.guidedsimulator.gui.ui.util.TextColor;
import it.unicam.pros.guidedsimulator.gui.ui.util.UIUtils;
import it.unicam.pros.guidedsimulator.gui.ui.util.css.*;
import it.unicam.pros.guidedsimulator.gui.ui.view.MainLayout;
import it.unicam.pros.guidedsimulator.gui.ui.view.ViewFrame;
import it.unicam.pros.guidedsimulator.gui.ui.components.Uploader;
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
import java.util.concurrent.*;


@Push(value = PushMode.MANUAL)
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
    private final Button downloadBtn;
    private VerticalLayout form;
    private Select<String> currency;
    private EventLog log;
    private FlexBoxLayout statistics;
    private Row stats;
    private Button cancelCalcs;
    private ProgressBar bar;
    private Future<EventLog> future;
    private static ExecutorService executor
            = Executors.newSingleThreadExecutor();
    private Component proBar;
    private Set<Binder> binders = new HashSet<Binder>();
    private UI ui;
    private FileDownloadWrapper link;
    private QuickPopup popup;

    public BPMNWhatIfAnalysisView(){


        form = new VerticalLayout();
        genBtn = new Button("Generate Log");
        upl = new Uploader("application/octet-stream",".bpmn");
        tauField = new NumberField("% of Precision");
        maxTraces = new IntegerField("Max trace number");
        downloadBtn = new Button("Download Log");
        cancelCalcs = new Button("", new Icon(VaadinIcon.CLOSE_CIRCLE));
        cancelCalcs.setIconAfterText(true);
        bar = new ProgressBar();

        setViewContent(createContent());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        ui = attachEvent.getUI();
    }

    private Component createContent() {
        Component inputs = createInputs();
        Component actions = createActions();
        proBar = createProBar();
        Component form = createForm();
        Component stats = createStats();
        createPopup();
        stats.setVisible(false);
        proBar.setVisible(false);
        FlexBoxLayout content = new FlexBoxLayout(inputs, actions, proBar, stats, form);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setFlexDirection(FlexDirection.COLUMN);
        return content;
    }

    private void createPopup() {
        VerticalLayout content = new VerticalLayout();
        popup = new QuickPopup(genBtn.getElement(),content);
        Button b = new Button("",new Icon(VaadinIcon.CLOSE));
        b.addClickListener(event1 -> {
            popup.hide();
        });
        b.setWidth("20%");
        b.getStyle().set("margin-left", "80%");
        Text t = new Text("Check simulation options.");
        content.getStyle().set("color", "white");
        content.getStyle().set("background" , "#233348");
        content.add(b,t);
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

    private void setBoxLayout(FlexBoxLayout l){
        l.setBoxSizing(BoxSizing.BORDER_BOX);
        l.setDisplay(Display.BLOCK);
        l.setMargin(Top.L);
        l.setMaxWidth(MAX_WIDTH);
        l.setPadding(Horizontal.RESPONSIVE_L);
        l.setWidthFull();
    }

    private Row createRow(){
        Row r = new Row();
        UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, r);
        UIUtils.setBorderRadius(BorderRadius.S, r);
        UIUtils.setShadow(Shadow.XS, r);
        return r;
    }

    private Component createProBar() {
        cancelCalcs.setWidth("15%");
        cancelCalcs.addClickListener(event -> {
            GuidedSimulator.setInterrupt(true);
            while (!future.isDone()){
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logStream = LogIO.getAsStream(log);
            populateStatistics();
            statistics.setVisible(true);
            downloadBtn.setEnabled(true);
            link.setVisible(true);
            GuidedSimulator.setInterrupt(false);
            proBar.setVisible(false);
        });
        bar.setIndeterminate(true);
        FlexBoxLayout proBar = new FlexBoxLayout(
                new HorizontalLayout(bar, cancelCalcs));
        setBoxLayout(proBar);
        return proBar;
    }


    private Component createInputs() {
        FlexBoxLayout inputs = new FlexBoxLayout(
                createHeader(VaadinIcon.INPUT, "Inputs"),
                createInputsComponents());
        setBoxLayout(inputs);
        return inputs;
    }

    private Component createInputsComponents() {
        Row inputs = createRow();

        //Uploader
        upl.getUploadComponent().addSucceededListener(event -> {
            refactor();
            mi = Bpmn.readModelFromStream(upl.getStream());
            o  = ScenariosParamsExtractor.extractParams(mi);
            populateForm(form, o);
            genBtn.setEnabled(true);
            proBar.setVisible(false);
            if(future!=null){
                cancelCalcs.click();
            }
        });

        //Tau field
        tauField.setValue(100d);
        tauField.setHasControls(true);
        tauField.setMin(0);
        tauField.setMax(100);
        Binder<NumberField> bind = new Binder<NumberField>();
        binders.add(bind);
        bind.forField(tauField).withValidator(val -> val >= 0 && val <= 100,
                "Select a value between 0 and 100.").bind(NumberField::getValue, NumberField::setValue);

        maxTraces.setValue(10000);
        maxTraces.setMin(1);
        Binder<IntegerField> bind1 = new Binder<IntegerField>();
        binders.add(bind1);
        bind1.forField(maxTraces).withValidator(val -> val > 0,
                "Select a value greater than 0.").bind(IntegerField::getValue, IntegerField::setValue);

        inputs.add(upl.getUploadComponent(), tauField, maxTraces);
        return inputs;
    }


    private Component createActions() {
        FlexBoxLayout actions = new FlexBoxLayout(
                createHeader(VaadinIcon.HAND, "Actions"),
                createActionsComponents());
        setBoxLayout(actions);
        return actions;
    }

    private Component createActionsComponents() {
        Row actions = createRow();

        //Log generation button
        genBtn.setEnabled(false);
        genBtn.setWidth("100%");
        genBtn.addClickListener(event -> {
            if(invalidInputs()){
                popup.show();
                return;
            }
            downloadBtn.setEnabled(false);
            link.setVisible(false);
            proBar.setVisible(true);
            future = wifThread(ui);
        });

        //Download button
        downloadBtn.setEnabled(false);
        downloadBtn.setWidth("100%");
        link = new FileDownloadWrapper("log.xes", () -> {
            return logStream.toByteArray();
        });
        link.wrapComponent(downloadBtn);
        link.setWidth("100%");
        link.setVisible(false);
        actions.add(genBtn ,link);
        return actions;
    }

    private boolean invalidInputs() {
        for(Binder b : binders){
            if (!b.isValid()){
                return true;
            }
        }
        return false;
    }



    private Future<EventLog> wifThread(UI ui){
        return executor.submit(() -> {
            log = GuidedSimulator.whatif(mi, getChoicesParams(), getActivityCosts(), tauField.getValue(), maxTraces.getValue());
            logStream = LogIO.getAsStream(log);
            GuidedSimulator.setInterrupt(false);
            ui.access(() -> {
                populateStatistics();
                statistics.setVisible(true);
                downloadBtn.setEnabled(true);
                link.setVisible(true);
                proBar.setVisible(false);
                ui.push();
            });
            return log;
        });
    }


    private Map<String, Double> getActivityCosts() {
        Map<String, Double> ret = new HashMap<String, Double>();
        for (String t : activities.keySet()){
            ret.put(t, activities.get(t).getValue());
        }
        return ret;
    }

    private Component createForm() {
        FlexBoxLayout options = new FlexBoxLayout(
                createHeader(VaadinIcon.HAND, "Options"),
                createFormComponents());
        setBoxLayout(options);
        return options;
    }

    private Component createFormComponents() {
        Row params = createRow();

        params.add(form);
        return params;
    }

    private Component createStats() {
        stats = createRow();
        statistics = new FlexBoxLayout(
                createHeader(VaadinIcon.CHART, "Statistics"),
                stats);
        setBoxLayout(statistics);
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
                Binder<NumberField> bind = new Binder<NumberField>();
                binders.add(bind);
                bind.forField(tmp).withValidator(val -> val > 0,
                        "Cost must be positive.").bind(NumberField::getValue, NumberField::setValue);
                activities.put(tId, tmp);
                layout.add(new HorizontalLayout(new H5(acts.get(tId)), tmp));
            }
            Map<String, List<String>> gatws = proc.getChoices();
            layout.add(new H4("Choices:"));
            for(String gId : gatws.keySet()){
                Text t = new Text(gId);
                layout.add(t);
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

                for(NumberField f : correlateChoices.keySet()){
                    Binder<NumberField> bind = new Binder<NumberField>();
                    binders.add(bind);
                    XorPercentageValidator xorV = new XorPercentageValidator(f, correlateChoices);
                    Binder.Binding<NumberField, Double> returningBind =
                            bind.forField(f).withValidator(xorV).bind(NumberField::getValue, NumberField::setValue);
                    for(NumberField f1 : correlateChoices.get(f)){
                        if (f.equals(f1)){continue;}
                        f1.addValueChangeListener(event -> {
                            returningBind.validate();
                        });
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
