package it.unicam.pros.purple.gui.ui.view.conformance;


import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
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
import it.unicam.pros.purple.gui.ui.layout.size.Bottom;
import it.unicam.pros.purple.gui.ui.layout.size.Horizontal;
import it.unicam.pros.purple.gui.ui.layout.size.Right;
import it.unicam.pros.purple.gui.ui.layout.size.Top;
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
import it.unicam.pros.purple.gui.util.Constants;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.utils.LogIO;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


//@Push(value = PushMode.MANUAL)
@Route(value="conformance/fixed_align_cost", layout = MainLayout.class)
@PageTitle("Conformance Checking via Fixed Align Cost | "+ Constants.shortName)
public class FixedAlignCostView extends ViewFrame {

    public static final String MAX_WIDTH = "1024px";
    private final Button viewModelBtn;
    private final NumberField alignCost;
    private ByteArrayOutputStream logStream;
    private Button genBtn;
    private Uploader upl;
    private NumberField tauField;
    private IntegerField maxTraces;
    private final Button downloadBtn;
    private VerticalLayout form;
    private EventLog log;
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
    private InputStream modelStream;
    private boolean isBPMN;

    public FixedAlignCostView(){

        setId("Conformance Checking via Fixed Align Cost");
        form = new VerticalLayout();
        form.setMaxHeight("500px");
        form.getStyle().set("overflow", "auto");
        genBtn = new Button("Generate Log");
        upl = new Uploader("application/octet-stream",".bpmn", ".pnml");
        tauField = new NumberField("% of Precision");
        alignCost = new NumberField("Alignment cost");
        maxTraces = new IntegerField("Traces number");
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
            modelStream = upl.getStream();
            isBPMN = event.getFileName().contains(".bpmn");
            genBtn.setEnabled(true);
            alignCost.setEnabled(true);
            proBar.setVisible(false);
            if(future!=null){
                cancelCalcs.click();
            }
            viewModelBtn.setVisible(true);
        });
        alignCost.setValue(0d);
        alignCost.setMin(0d);
        Binder<NumberField> bind = new Binder<NumberField>();
        binders.add(bind);
        bind.forField(alignCost).withValidator(val -> val >= 0,
                "Select a value greater than 0.").bind(NumberField::getValue, NumberField::setValue);

        //Tau field
        tauField.setValue(100d);
        tauField.setHasControls(true);
        tauField.setMin(0);
        tauField.setMax(100);
        bind = new Binder<NumberField>();
        binders.add(bind);
        bind.forField(tauField).withValidator(val -> val >= 0 && val <= 100,
                "Select a value between 0 and 100.").bind(NumberField::getValue, NumberField::setValue);

        maxTraces.setValue(10000);
        maxTraces.setMin(1);
        Binder<IntegerField> bind1 = new Binder<IntegerField>();
        binders.add(bind1);
        bind1.forField(maxTraces).withValidator(val -> val > 0,
                "Select a value greater than 0.").bind(IntegerField::getValue, IntegerField::setValue);



        inputs.add(upl.getUploadComponent(), alignCost, tauField, maxTraces);
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
            } catch (ExecutionException e) {
                showError(e.toString());
            } catch (InterruptedException e) {
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
            if(isBPMN){
                log = PURPLE.bpmnAlignCostConformance(modelStream, alignCost.getValue(), maxTraces.getValue(), tauField.getValue());
            }else{
                log = PURPLE.pnmlAlignCostConformance(modelStream, alignCost.getValue(), maxTraces.getValue(), tauField.getValue());
            }
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

}
