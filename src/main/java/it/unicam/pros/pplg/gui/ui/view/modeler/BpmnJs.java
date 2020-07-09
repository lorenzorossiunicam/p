package it.unicam.pros.pplg.gui.ui.view.modeler;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.page.Page;
import it.unicam.pros.pplg.gui.ui.components.FlexBoxLayout;
import it.unicam.pros.pplg.gui.ui.util.css.FlexDirection;


@JavaScript("./modeler.js")
@CssImport("./styles/bpmnjs/diagram-js.css")
@CssImport("./styles/bpmnjs/bpmn.css")
public class BpmnJs {


    private static Div ppanel;

    public static Component getComponent() {
        Div canvas = new Div();
        canvas.setId("canvas");
        canvas.setWidth("100%");
        canvas.setHeight("500px");
        canvas.getStyle().set("resize", "vertical");
        canvas.getStyle().set("overflow", "auto");
        canvas.getStyle().set("border", "solid");
        ppanel = new Div();
        ppanel.setId("js-properties-panel");
        FlexBoxLayout content = new FlexBoxLayout(canvas, ppanel);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setWidthFull();
        return content;
    }

    public static void createModeler(String xml){
        xml = xml.replaceAll("\\r|\\n", "");
        Page page = UI.getCurrent().getPage();
        page.executeJs("createModeler('"+xml+"')");
    }

    public static void createViewer(String xml){
        xml = xml.replaceAll("\\r|\\n", "");
        Page page = UI.getCurrent().getPage();
        page.executeJs("createModeler('"+xml+"');");
        ppanel.getStyle().set("display","none");
    }

    public static void setColor(String elId, String fill, String stroke){
        Page page = UI.getCurrent().getPage();
        page.executeJs("var toChange;\n" +
                "bpmnjs_elReg.forEach(function(e){\n" +
                "        if (e.id == '"+elId+"') {\n" +
                "        toChange = e;\n" +
                "       } \n" +
                "});\n" +
                "console.log(bpmnjs);" +
                "bpmnjs.get('modeling').setColor([toChange],{\n" +
                "        stroke: '"+stroke+"',\n" +
                "        fill: '"+fill+"'\n" +
                "});    ");

    }
}

