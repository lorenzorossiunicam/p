package it.unicam.pros.purple.gui.ui.view.modeler;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.page.Page;
import it.unicam.pros.purple.gui.ui.components.FlexBoxLayout;


@JavaScript("./modeler.js")
@JavaScript("./draggable.js")
@CssImport("./styles/bpmnjs/diagram-js.css")
@CssImport("./styles/bpmnjs/bpmn.css")
@CssImport("./styles/bpmnjs/canvas.css")
public class BpmnJs {


    private static Div ppanel;
    private static Div canvas;
    private static FlexBoxLayout content;
    private boolean done = false, hidden = true;

    public BpmnJs(){
        createComponent();
    }

    private void populateComponent(boolean modeler){
        if(done) return;
        instantiateModeler();
        if(!modeler){
            disableModeling();
        }
    }

    private void createComponent(){
        canvas = new Div();
        canvas.setId("canvas");
        ppanel = new Div();
        ppanel.setId("js-properties-panel");

        canvas.add(ppanel);
        content = new FlexBoxLayout(canvas);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setWidthFull();
    }


//    private void hideShow() {
//        if (hidden){
//            canvas.getStyle().set("bottom", "0px");
//            canvas.getStyle().set("right", "0px");
//            close.setIcon(new Icon(VaadinIcon.CARET_DOWN));
//        }else{
//            canvas.getStyle().set("bottom", "-460px");
//            canvas.getStyle().set("right", "-750px");
//            close.setIcon(new Icon(VaadinIcon.CARET_UP));
//        }
//        hidden = !hidden;
//    }

    public Component getComponent() {
        return content;
    }

    private void instantiateModeler(){
        Page page = UI.getCurrent().getPage();
        page.executeJs("createModeler();"); //+
                //"dragElement(document.getElementById(\"canvas\"));");
        done = true;
    }

    public void loadDiagram(String xml, boolean modeler){
        populateComponent(modeler);
        xml = xml.replaceAll("\\r|\\n", "");
        Page page = UI.getCurrent().getPage();
        page.executeJs("bpmnjs.importXML('"+xml+"', function (err) {\n" +
                "        if (!err) {\n" +
                "            bpmnjs.get('canvas').zoom('fit-viewport');\n" +
                "        } else {\n" +
                "            sessionStorage.clear()\n" +
                "            console.log('something went wrong:', err);\n" +
                "        }\n" +
                "    });");
    }

    private void disableModeling() {
        ppanel.getStyle().set("display","none");
        Page page = UI.getCurrent().getPage();
        page.executeJs("document.getElementsByClassName(\"djs-palette\")[0].style.display = \"none\";");
        page.executeJs("document.getElementsByClassName(\"djs-context-pad\")[0].style.display = \"none\";");
    }

    public void setColor(String elId, String fill, String stroke){
        Page page = UI.getCurrent().getPage();
        page.executeJs("var toChange;\n" +
                "bpmnjs_elReg.forEach(function(e){\n" +
                "        if (e.id == '"+elId+"') {\n" +
                "        toChange = e;\n" +
                "       } \n" +
                "});\n" +
                "bpmnjs.get('modeling').setColor([toChange],{\n" +
                "        stroke: '"+stroke+"',\n" +
                "        fill: '"+fill+"'\n" +
                "});    ");

    }
}

