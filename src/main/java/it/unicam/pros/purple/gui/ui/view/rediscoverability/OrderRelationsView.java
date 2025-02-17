package it.unicam.pros.purple.gui.ui.view.rediscoverability;


import com.awesomecontrols.quickpopup.QuickPopup;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
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
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//@Push(value = PushMode.MANUAL)
@Route(value="discovery/order_relations", layout = MainLayout.class)
@PageTitle("BPMN process discovery via order relations | "+ Constants.shortName)
public class OrderRelationsView extends ViewFrame {

    public static final String MAX_WIDTH = "1024px";
    private final Uploader u;
    private final ProgressBar bar;
    private InputStream model;
    private NumberField tauField;
    private ByteArrayOutputStream logStream;
    private Button genBtn, downloadBtn;
    private Map<String, Object> algoMap;
    private Future<ByteArrayOutputStream> future;
    private static ExecutorService executor
            = Executors.newSingleThreadExecutor();
    private Component proBar;
    private Set<Binder> binders = new HashSet<Binder>();
    private UI ui;
    private Button cancelCalcs;
    private QuickPopup popup;
    private FileDownloadWrapper link;
    private boolean isBPMN = false;

    public OrderRelationsView(){
        setId("Process discovery via order relations");
        u = new Uploader("application/octet-stream", ".bpmn", ".pnml");
        tauField = new NumberField("% of Completeness");
        genBtn = new Button("Generate log");
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
        createPopup();
        proBar = createProBar();
        proBar.setVisible(false);

        FlexBoxLayout content = new FlexBoxLayout(inputs, actions, proBar);
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
            downloadBtn.setEnabled(true);
            link.setVisible(true);
            PURPLE.setInterrupt(false);
            proBar.setVisible(false);
        });
        bar.setIndeterminate(true);
        FlexBoxLayout proBar = new FlexBoxLayout(new HorizontalLayout(bar, cancelCalcs));
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

        u.getUploadComponent().addSucceededListener(event -> {
            model = u.getStream();
            isBPMN = event.getFileName().contains(".bpmn");
            genBtn.setEnabled(true);
            proBar.setVisible(false);
            if(future!=null){
                cancelCalcs.click();
            }
        });

        tauField.setValue(100d);
        tauField.setHasControls(true);
        tauField.setMin(0);
        tauField.setMax(100);
        Binder<NumberField> bind = new Binder<NumberField>();
        binders.add(bind);
        bind.forField(tauField).withValidator(val -> val >= 0 && val <= 100,
                "Select a value between 0 and 100.").bind(NumberField::getValue, NumberField::setValue);


        inputs.add(u.getUploadComponent(), tauField);
        return inputs;
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

    private Component createActions() {
        FlexBoxLayout actions = new FlexBoxLayout(
                createHeader(VaadinIcon.HAND, "Actions"),
                createActionsComponents());
        setBoxLayout(actions);
        return actions;
    }

    private Component createActionsComponents() {
        Row actions = createRow();

        genBtn.setEnabled(false);
        genBtn.addClickListener(event -> {
            if(invalidInputs()){
                popup.show();
                return;
            }
            downloadBtn.setEnabled(false);
            link.setVisible(false);
            proBar.setVisible(true);
            future = redThread(ui);
            try {
                future.get();
            } catch (Exception e) {
                showError(e.toString());
            }
        });

        downloadBtn.setEnabled(false);
        downloadBtn.setWidth("100%");
        link = new FileDownloadWrapper("log.xes", () -> {
            return logStream.toByteArray();
        });
        link.wrapComponent(this.downloadBtn);
        link.setWidth("100%");
        actions.add(genBtn, link);
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

    private Future<ByteArrayOutputStream> redThread(UI ui){
        return executor.submit(() -> {
            if(isBPMN){
                logStream = PURPLE.bpmnRediscoverability(model, tauField.getValue());
            }else{
                logStream = PURPLE.pnmlRediscoverability(model, tauField.getValue());
            }


            PURPLE.setInterrupt(false);
            ui.access(() -> {
                downloadBtn.setEnabled(true);
                link.setVisible(true);
                proBar.setVisible(false);
                ui.push();
            });
            return logStream;
        });
    }

    public String[] getNames(Class<? extends Enum<?>> e) {
        this.algoMap = new HashMap<String, Object>();
        for (Object o : e.getEnumConstants()){
            this.algoMap.put(o.toString(),o);
        }
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }


}
