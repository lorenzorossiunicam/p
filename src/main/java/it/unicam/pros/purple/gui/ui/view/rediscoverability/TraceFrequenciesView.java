package it.unicam.pros.purple.gui.ui.view.rediscoverability;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import it.unicam.pros.purple.PURPLE;
import it.unicam.pros.purple.gui.ui.components.FlexBoxLayout;
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
import it.unicam.pros.purple.gui.ui.view.whatif.XorPercentageValidator;
import it.unicam.pros.purple.gui.util.Constants;
import it.unicam.pros.purple.util.Couple;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.utils.LogIO;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//@Push(value = PushMode.MANUAL)
@Route(value="discovery/trace_frequencies", layout = MainLayout.class)
@PageTitle("BPMN process discovery via traces frequencies | "+ Constants.shortName)
public class TraceFrequenciesView extends ViewFrame {
    public static final String MAX_WIDTH = "1024px";
    private ByteArrayOutputStream logStream;
    private Set<Trace> o;
    private Map<Trace,Double> frequencies = new HashMap<>();
    private Map<NumberField, Set<NumberField>> correlateChoices = new HashMap<NumberField, Set<NumberField>>();
    private BpmnModelInstance mi;
    private Button genBtn;
    private Uploader upl;
    private NumberField tauField;
    private IntegerField maxTraces;
    private final Button downloadBtn;
    private VerticalLayout form;
    private EventLog log;
    private Button cancelCalcs;
    private ProgressBar bar;
    private Future<EventLog> future;
    private static ExecutorService executor
            = Executors.newSingleThreadExecutor();
    private Component proBar;
    private Set<Binder> binders = new HashSet<Binder>();
    private UI ui;
    private FileDownloadWrapper link;
    private String xml;
    private Map<Trace, int[]> cycles;
    private Map<Trace, int[]> loopFrequencies = new HashMap<Trace, int[]>();

    public TraceFrequenciesView(){

        setId("Process discovery via trace frequencies");
        form = new VerticalLayout();
        form.setMaxHeight("500px");
        form.getStyle().set("overflow", "auto");
        genBtn = new Button("Generate Log");
        upl = new Uploader("application/octet-stream",".bpmn");
        tauField = new NumberField("% of Precision");
        maxTraces = new IntegerField("Min trace number");
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
        proBar.setVisible(false);
        FlexBoxLayout content = new FlexBoxLayout(inputs, actions, proBar, form);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
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
                mi = Bpmn.readModelFromStream(modelStream);
            } catch (Exception e) {
                showError(e.toString());
            }
            Couple<Set<Trace>, Map<Trace, int[]>> couple = null;
            try {
                couple = PURPLE.bpmnSimpleLog(mi);
            } catch (Exception e) {
                showError(e.toString());
            }
            o = couple.getE();
            cycles = couple.getV();
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
        maxTraces.setValue(1000);
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
            downloadBtn.setEnabled(false);
            link.setVisible(false);
            proBar.setVisible(true);
            future = wifThread(ui);
            try {
                future.get();
            } catch (Exception e) {
                showError(e.toString());
            }
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




    private Future<EventLog> wifThread(UI ui){

        return executor.submit(() -> {
            log = PURPLE.bpmnCustomTraceFrequency(mi,frequencies, loopFrequencies, maxTraces.getValue(),tauField.getValue());
            logStream = LogIO.getAsStream(log);
            PURPLE.setInterrupt(false);
            ui.access(() -> {
                downloadBtn.setEnabled(true);
                link.setVisible(true);
                proBar.setVisible(false);
                ui.push();
            });
            return log;
        });
    }






    private void refactor() {
        form.removeAll();
    }


    private Component populateForm(VerticalLayout layout, Set<Trace> content) {
        layout.removeAll();
        double startFreq = 100.0/content.size();
        Set<NumberField> freqs = new HashSet<>();
        layout.add(new H3("Traces frequency"));
        for (Trace t : content){
            int [] cBounds = cycles.get(t);
            String sst = onlyActs(t, cBounds);
            NumberField tmp = new NumberField();
            tmp.setMin(0);
            tmp.setValue(startFreq);
            frequencies.put(t,startFreq);
            freqs.add(tmp);
            IntegerField tmp2 = null;
            Label tmp2Lab = null;
            if (cBounds != null){
                //sst = sst.substring(0,cBounds[0]-1)+ "(" + sst.substring(cBounds[0], cBounds[1]-1) + ")" + sst.substring(cBounds[1], sst.length()-1);
                tmp2 = new IntegerField();
                tmp2.setMin(1);
                tmp2.setValue(1);
                tmp2Lab = new Label("Max repetitions");
                loopFrequencies.put(t,new int[]{cBounds[0], cBounds[1], 1});
                tmp2.addValueChangeListener(e -> {
                loopFrequencies.put(t,new int[]{cBounds[0], cBounds[1], e.getValue()});
                });
            }
            if (tmp2 != null && tmp2Lab != null){
                layout.add(new HorizontalLayout(new Label(sst), tmp, new Label("%"), tmp2, tmp2Lab));
            }else {
                layout.add(new HorizontalLayout(new Label(sst), tmp, new Label("%")));
            }


            tmp.addValueChangeListener(e -> {
                frequencies.put(t,e.getValue());
            });
        }
        for(NumberField f : freqs){


            correlateChoices.put(f, new HashSet<NumberField>());
            for (NumberField f1 : freqs){
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
//        if(!cycles.isEmpty())
//            layout.add(new H3("Loops length"));
//        for (List<String> c : cycles){
//            IntegerField tmp = new IntegerField();
//            tmp.setMin(0);
//            tmp.setValue(2);
//            String cS = "";
//            for(int i  = 0; i<c.size(); i++){
//                if(i==c.size()-1){
//                    cS += c.get(i);
//                }else {
//                    cS += c.get(i)+", ";
//                }
//            }
//            layout.add(new HorizontalLayout(new Label(cS), tmp, new Label("Mean")));
//            tmp.addValueChangeListener(e -> {
//                loopFrequencies.put(c,e.getValue());
//            });
//        }
        return layout;
    }

    private String onlyActs(Trace t, int[] cBounds) {
        String ret = "< ";
        int i = 0;
        for(Event e : t.getTrace()){
            i++;
            if (cBounds!= null && i== cBounds[0]+cBounds[2]+1){
                ret += "⟲( ";
            }
            ret += e.getEventName();
            if (cBounds!= null && i== cBounds[1]+cBounds[2]+cBounds[3]){
                ret += " )";
            }
            if(i!= t.getTrace().size()){
                ret+= ", ";
            }
        }
        return ret+" >";
    }
}
