package it.unicam.pros.guidedsimulator.gui.ui.view.rediscoverability;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.unicam.pros.guidedsimulator.evaluator.purpose.rediscoverability.Rediscoverability;
import it.unicam.pros.guidedsimulator.gui.ui.view.MainLayout;
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
import java.util.logging.Logger;

@Route(value="rediscoverability/b", layout = MainLayout.class)
@PageTitle("BPMN Rediscoverability | GuidedSimulator")
public class RV extends VerticalLayout {

    public static final String MAX_WIDTH = "1024px";

    private InputStream model;
    private Select<String> algoSelect;
    private NumberField tauField;
    private ByteArrayOutputStream logStream;
    private com.vaadin.flow.component.button.Button genBtn, downloadBtn;
    private Map<String, Object> algoMap;
    private final static Logger logger =
            Logger.getLogger(BPMNRediscoverabilityView.class.getName());
    private String filename;

    public String[] getNames(Class<? extends Enum<?>> e) {
        this.algoMap = new HashMap<String, Object>();
        for (Object o : e.getEnumConstants()){
            this.algoMap.put(o.toString(),o);
        }
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

    public RV() {
        LogManager.getLogManager().reset();

        com.vaadin.flow.component.textfield.TextArea console = new TextArea();
        SimLogAppender.setArea(console);

        //Title and description
        Text title = new Text("BPMN - Rediscoverability");
        Text desc = new Text("This utility permits to generate a log suitable for rediscoverability purposes...\n Upload!!!");

        //Input component
        com.vaadin.flow.component.orderedlayout.HorizontalLayout inputs = new HorizontalLayout();


        final InputStream[] model = new InputStream[1];
        Uploader u = new Uploader("application/octet-stream", ".bpmn");
        u.getUploadComponent().addSucceededListener(event -> {
            model[0] = u.getStream();
            genBtn.setEnabled(true);
        });

        Select<String> algoSelect = new Select<>();
        String[] algos = getNames(Rediscoverability.RediscoverabilityAlgo.class);
        algoSelect.setItems(algos);
        algoSelect.setValue(algos[0]);


        NumberField tauField = new NumberField("% of Completeness");
        tauField.setValue(100d);
        tauField.setHasControls(true);
        tauField.setMin(0);
        tauField.setMax(100);


        this.genBtn = new Button("Generate log");
        this.genBtn.setEnabled(false);
        this.genBtn.addClickListener(event -> {

            logStream = GuidedSimulator.rediscoverability(model[0], (Rediscoverability.RediscoverabilityAlgo) algoMap.get(algoSelect.getValue()),tauField.getValue()/100);

            this.downloadBtn.setEnabled(true);
        });



        this.downloadBtn = new com.vaadin.flow.component.button.Button("Download Log");
        this.downloadBtn.setEnabled(false);
        FileDownloadWrapper link = new FileDownloadWrapper("log.xes", () -> {
            System.out.println(logStream.toByteArray());
            return logStream.toByteArray();
        });
        link.wrapComponent(this.downloadBtn);

        inputs.add(u.getUploadComponent(), algoSelect, tauField, genBtn);
        inputs.setAlignItems(FlexComponent.Alignment.END);

        add(title, desc,  inputs, link, console);


    }
}
