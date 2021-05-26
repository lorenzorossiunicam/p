package it.unicam.pros.purple.gui.ui.view.collabwhatif;


import com.awesomecontrols.quickpopup.QuickPopup;
import com.vaadin.annotations.Push;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.shared.communication.PushMode;
import it.unicam.pros.purple.PURPLE;
import it.unicam.pros.purple.evaluator.purpose.whatifanalysis.util.Scenario;
import it.unicam.pros.purple.evaluator.purpose.whatifanalysis.util.ScenariosParamsExtractor;
import it.unicam.pros.purple.gui.ui.components.FlexBoxLayout;
import it.unicam.pros.purple.gui.ui.components.GChart;
import it.unicam.pros.purple.gui.ui.components.Uploader;
import it.unicam.pros.purple.gui.ui.layout.size.*;
import it.unicam.pros.purple.gui.ui.util.IconSize;
import it.unicam.pros.purple.gui.ui.util.LumoStyles;
import it.unicam.pros.purple.gui.ui.util.TextColor;
import it.unicam.pros.purple.gui.ui.util.UIUtils;
import it.unicam.pros.purple.gui.ui.util.css.BorderRadius;
import it.unicam.pros.purple.gui.ui.util.css.BoxSizing;
import it.unicam.pros.purple.gui.ui.util.css.Display;
import it.unicam.pros.purple.gui.ui.util.css.Shadow;
import it.unicam.pros.purple.gui.ui.view.MainLayout;
import it.unicam.pros.purple.gui.ui.view.ViewFrame;
import it.unicam.pros.purple.gui.ui.view.modeler.BpmnJs;
import it.unicam.pros.purple.gui.ui.view.whatif.XorPercentageValidator;
import it.unicam.pros.purple.gui.util.Constants;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.utils.LogIO;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Push(value = PushMode.MANUAL)
@Route(value="collabwhatif/bpmn", layout = MainLayout.class)
@PageTitle("BPMN Collaboration What If Analysis | "+ Constants.shortName)
public class BPMNCollabWhatIfAnalysisView extends ViewFrame {

    public static final String MAX_WIDTH = "1024px";
    private final Button viewModelBtn;
    private ByteArrayOutputStream logStream;
    private Map<String, Scenario> o;
    private Map<String, NumberField> activities = new HashMap<String, NumberField>();
    private Map<String, NumberField> durations = new HashMap<String, NumberField>();
    private Map<String, Map<String, NumberField>> choices = new HashMap<String, Map<String, NumberField>>();
    private Map<NumberField, Set<NumberField>> correlateChoices = new HashMap<NumberField, Set<NumberField>>();
    private BpmnModelInstance mi;
    private Button genBtn;
    private Uploader upl;
    private NumberField tauField;
    private IntegerField maxTraces;
    private final Button downloadBtn;
    private VerticalLayout form;
    private DateTimePicker  initDate = new DateTimePicker(LocalDateTime.now());

    private Select<String> currency;
    private EventLog log;
    private FlexBoxLayout statistics;
    private Row stats;
    private Button cancelCalcs;
    private Button preview;
    private ProgressBar bar;
    private Future<EventLog> future;
    private static ExecutorService executor
            = Executors.newSingleThreadExecutor();
    private Component proBar;
    private Set<Binder> binders = new HashSet<Binder>();
    private UI ui;
    private FileDownloadWrapper link;
    private QuickPopup popup;
    private String xml;
    private String lastColEl = null;
    private BpmnJs bpmnjs;
    private Row options;

    public BPMNCollabWhatIfAnalysisView(){

        setId("Collaboration What-if Analysis - BPMN");
        form = new VerticalLayout();
        form.setMaxHeight("500px");
        form.getStyle().set("overflow", "auto");
        genBtn = new Button("Generate Log");
        upl = new Uploader("application/octet-stream",".bpmn");
        tauField = new NumberField("% of Precision");
        maxTraces = new IntegerField("Max trace number");
        downloadBtn = new Button("Download Log");
        cancelCalcs = new Button("", new Icon(VaadinIcon.CLOSE_CIRCLE));
        cancelCalcs.setIconAfterText(true);
        bar = new ProgressBar();
        viewModelBtn = new Button("", new Icon(VaadinIcon.EYE));
        preview = new Button(new Icon(VaadinIcon.EYE));
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
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
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
        content.getStyle().set("background" , "#8c1873");
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
            PURPLE.setInterrupt(true);
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
            PURPLE.setInterrupt(false);
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
            InputStream modelStream = upl.getStream();

            try {
                xml = IOUtils.toString(modelStream, "UTF-8");
                modelStream = IOUtils.toInputStream(xml, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            mi = Bpmn.readModelFromStream(modelStream);
            o  = ScenariosParamsExtractor.extractParams(mi);
            populateForm(form, o);
            genBtn.setEnabled(true);
            proBar.setVisible(false);
            if(future!=null){
                cancelCalcs.click();
            }
            viewModelBtn.setVisible(true);
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
            long init = initDate.getValue().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            log = PURPLE.whatifwithtime(mi, getChoicesParams(), getActivityCosts(), getActivityDurations(), init, tauField.getValue(), maxTraces.getValue());
            logStream = LogIO.getAsStream(log);
            PURPLE.setInterrupt(false);
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

    private Map<String, Long> getActivityDurations() {
        Map<String, Long> ret = new HashMap<String, Long>();
        for (String t : durations.keySet()){
            ret.put(t, (long) (durations.get(t).getValue()*1000));
        }
        return ret;
    }

    private Component createForm() {
        bpmnjs = new BpmnJs();
        options = new Row(createFlexBoxLayout(bpmnjs.getComponent(), "Model preview", VaadinIcon.SPLIT),
                createFlexBoxLayout(form, "Options", VaadinIcon.HAND));
        options.addClassName(LumoStyles.Margin.Top.XL);
        UIUtils.setMaxWidth("2048" , options);
        options.setWidthFull();
        upl.getUploadComponent().addSucceededListener(event -> {

            bpmnjs.loadDiagram(xml, false);
        });
        return options;
    }

    private Component createFlexBoxLayout(Component c,  String name, VaadinIcon i){
        FlexBoxLayout header = createHeader(i, name);
        FlexBoxLayout reports = new FlexBoxLayout(header, c);
        reports.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        reports.setPadding(Bottom.XL, Left.RESPONSIVE_L);
        return reports;
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
        Double minD = null, maxD = 0.0, avgD = 0.0;
        int nTraces = log.size();
        for(Trace t : log.getTraces()){
            double tCost = 0.0;
            long iTime = 0, eTime = 0;
            boolean first = true;
            for (Event e : t.getTrace()){
                if(first) {
                    iTime = e.getTimestamp().getTime();
                    first = false;
                }
                if (e.getCost()!=null) {
                    tCost += e.getCost();
                }
                if (e.getTimestamp()!=null) {
                    if(e.getTimestamp().getTime()>eTime){
                        eTime = e.getTimestamp().getTime();
                    }
                }
            }
            double elapse = eTime - iTime;
            if (minD == null || elapse < minD){
                minD = elapse;
            }
            if (elapse > maxD){
                maxD = elapse;
            }
            avgD += elapse;

            if (min == null || tCost < min){
                min = tCost;
            }
            if (tCost > max){
                max = tCost;
            }
            avg += tCost;
        }
        avg = avg / nTraces;
        avgD = avgD / nTraces;
        H4 results = new H4("Min: "+min+" Max: "+max+" Average: "+avg);
        Map<String, Double> d = new HashMap<String, Double>(3);
        d.put("Min", min);
        d.put("Max", max);
        d.put("Avg", avg);
        GChart chart = new GChart("", "Cost", d);
        Map<String, Double> t = new HashMap<String, Double>(3);
        d.put("Min", minD);
        d.put("Max", maxD);
        d.put("Avg", avgD);
        GChart chart2 = new GChart("", "Duration", d);
        stats.add(new VerticalLayout(new H3("#Traces"),new H4(String.valueOf(nTraces)),new H3("Costs & Durations"),results, new HorizontalLayout(chart.getComponent(), chart2.getComponent())));

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
            layout.add(new H4("Activity costs (Euro):"));


            for(String tId : acts.keySet()){
                NumberField tmp = new NumberField();
                tmp.setMin(0);
                tmp.setValue(1.0);
                tmp.addFocusListener(event -> {
                    if(lastColEl != null)
                    bpmnjs.setColor(lastColEl,"", "");
                    lastColEl = tId;
                    bpmnjs.setColor(tId,"", "red");
                });
                Binder<NumberField> bind = new Binder<NumberField>();
                binders.add(bind);
                bind.forField(tmp).withValidator(val -> val > 0,
                        "Cost must be positive.").bind(NumberField::getValue, NumberField::setValue);
                activities.put(tId, tmp);
                H5 id = new H5(acts.get(tId));
                id.setWidth("300px");
                layout.add(new HorizontalLayout(id, tmp));
            }

            layout.add(new H4("Starting date:"));
            layout.add(new HorizontalLayout(initDate));
            layout.add(new H4("Activity durations (seconds):"));

            for(String tId : acts.keySet()){
                NumberField tmp = new NumberField();
                tmp.setMin(0);
                tmp.setValue(1.0);
                tmp.addFocusListener(event -> {
                    if(lastColEl != null)
                        bpmnjs.setColor(lastColEl,"", "");
                    lastColEl = tId;
                    bpmnjs.setColor(tId,"", "red");
                });
                Binder<NumberField> bind = new Binder<NumberField>();
                binders.add(bind);
                bind.forField(tmp).withValidator(val -> val > 0,
                        "Duration must be positive.").bind(NumberField::getValue, NumberField::setValue);
                durations.put(tId, tmp);
                H5 id = new H5(acts.get(tId));
                id.setWidth("300px");
                layout.add(new HorizontalLayout(id, tmp));
            }

            Map<String, List<String>> gatws = proc.getChoices();
            layout.add(new H4("Choices:"));
            for(String gId : gatws.keySet()){
                H4 t = new H4(gId);
                layout.add(t);
                List<String> sFlows = gatws.get(gId);
                choices.put(gId, new HashMap<String, NumberField>());
                Set<NumberField> tmp = new HashSet<NumberField>();
                for(String sFlow : sFlows){
                    NumberField f = new NumberField();
                    f.setValue(100.0/sFlows.size());
                    tmp.add(f);
                    f.addFocusListener(event -> {
                        if(lastColEl != null)
                            bpmnjs.setColor(lastColEl,"", "");
                        lastColEl =  sFlow;
                        bpmnjs.setColor(lastColEl,"", "red");
                    });
                    choices.get(gId).put(sFlow, f);
                    H5 id = new H5(sFlow);
                    id.setWidth("300px");
                    layout.add(new HorizontalLayout(id, f, new H5("%")));
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

}
