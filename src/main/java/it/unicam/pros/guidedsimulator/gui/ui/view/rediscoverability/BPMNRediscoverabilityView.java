package it.unicam.pros.guidedsimulator.gui.ui.view.rediscoverability;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.unicam.pros.guidedsimulator.evaluator.purpose.rediscoverability.Rediscoverability;
import it.unicam.pros.guidedsimulator.gui.ui.components.FlexBoxLayout;
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
import it.unicam.pros.guidedsimulator.gui.util.logger.SimLogAppender;
import it.unicam.pros.guidedsimulator.guidedsimulator.GuidedSimulator;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;


@Route(value="rediscoverability/bpmn", layout = MainLayout.class)
@PageTitle("BPMN Rediscoverability | GuidedSimulator")
public class BPMNRediscoverabilityView extends ViewFrame {

    public static final String MAX_WIDTH = "1024px";
    private final Uploader u;
    private InputStream model;
    private Select<String> algoSelect;
    private NumberField tauField;
    private ByteArrayOutputStream logStream;
    private Button genBtn, downloadBtn;
    private Map<String, Object> algoMap;

    public BPMNRediscoverabilityView(){

        u = new Uploader("application/octet-stream", ".bpmn");
        algoSelect = new Select<>();
        algoSelect.setLabel("Mining algorithm");
        tauField = new NumberField("% of Completeness");
        genBtn = new Button("Generate log");
        downloadBtn = new Button("Download Log");

        setViewContent(createContent());
    }

    private Component createContent() {
        Component inputs = createInputs();
        Component actions = createActions();
       // Component console = createConsole();

        FlexBoxLayout content = new FlexBoxLayout(inputs, actions);//,console);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setFlexDirection(FlexDirection.COLUMN);
        return content;
    }

    private Component createConsole() {
        TextArea console = new TextArea();
        SimLogAppender.setArea(console);
        FlexBoxLayout consoleBox = new FlexBoxLayout(
                createHeader(VaadinIcon.CODE, "Console"), console);
        consoleBox.setBoxSizing(BoxSizing.BORDER_BOX);
        consoleBox.setDisplay(Display.BLOCK);
        consoleBox.setMargin(Top.L);
        consoleBox.setMaxWidth(MAX_WIDTH);
        consoleBox.setPadding(Horizontal.RESPONSIVE_L);
        consoleBox.setWidthFull();
        return  consoleBox;
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

        u.getUploadComponent().addSucceededListener(event -> {
            model = u.getStream();
            genBtn.setEnabled(true);
        });

        String[] algos = getNames(Rediscoverability.RediscoverabilityAlgo.class);
        algoSelect.setItems(algos);
        algoSelect.setValue(algos[0]);

        tauField.setValue(100d);
        tauField.setHasControls(true);
        tauField.setMin(0);
        tauField.setMax(100);

        inputs.add(u.getUploadComponent(),algoSelect, tauField);
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

        genBtn.setEnabled(false);
        genBtn.addClickListener(event -> {
            logStream = GuidedSimulator.rediscoverability(model,
                    (Rediscoverability.RediscoverabilityAlgo) algoMap.get(algoSelect.getValue()),
                    tauField.getValue());
            downloadBtn.setEnabled(true);
        });

        downloadBtn.setEnabled(false);
        downloadBtn.setWidth("100%");
        FileDownloadWrapper link = new FileDownloadWrapper("log.xes", () -> {
            return logStream.toByteArray();
        });
        link.wrapComponent(this.downloadBtn);
        link.setWidth("100%");
        actions.add(genBtn, link);
        return actions;
    }

    public String[] getNames(Class<? extends Enum<?>> e) {
        this.algoMap = new HashMap<String, Object>();
        for (Object o : e.getEnumConstants()){
            this.algoMap.put(o.toString(),o);
        }
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }


}
