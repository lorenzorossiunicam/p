package it.unicam.pros.pplg.gui.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.unicam.pros.pplg.gui.ui.components.FlexBoxLayout;
import it.unicam.pros.pplg.gui.ui.layout.size.Horizontal;
import it.unicam.pros.pplg.gui.ui.layout.size.Uniform;
import it.unicam.pros.pplg.gui.ui.util.UIUtils;
import it.unicam.pros.pplg.gui.ui.util.css.FlexDirection;

@Route(value="", layout = MainLayout.class)
@PageTitle("Home | GuidedSimulator")
public class HomeView extends ViewFrame {

    public HomeView(){
        setId("Home");
        setViewContent(createContent());
    }

    private Component createContent() {
        Html intro = new Html("<p>This tool provides a novel log generation methodology that can be parametric on " +
                "the input process model language  and on the mining purpose, to produce artificial event logs. " +
                "The methodology is meant to ensure the possibility of simulating any kind of process model through " +
                "the implementation of several modeling language semantics  (e.g., BPMN, Petri net, EPC, WF-net), " +
                "and also the possibility to decide characteristics of the output event log according to the requirements" +
                " of a mining procedure. </p>");

        Div image = new Div();
        Image img = new Image(UIUtils.IMG_PATH + "arch.png", "");

        img.setAlt("g_si_arch");
        img.setWidth("100%");
        img.setHeight("auto");
        image.add(img);

        Row links = new Row();
        Anchor documentation = new Anchor("https://bitbucket.org/proslabteam/guidedsimulator/src/master/README.md", UIUtils.createButton("Read the documentation", VaadinIcon.EXTERNAL_LINK));
        Anchor starter = new Anchor("https://bitbucket.org/proslabteam/guidedsimulator/src/master/", UIUtils.createButton("Get the source code", VaadinIcon.EXTERNAL_LINK));
        Anchor pros = new Anchor("http://pros.unicam.it/", UIUtils.createButton("ProsLab Team", VaadinIcon.EXTERNAL_LINK));
        //documentation.setWidth("33%");
        documentation.setTarget("_blank");
        //starter.setWidth("33%");
        starter.setTarget("_blank");
        //pros.setWidth("33%");
        pros.setTarget("_blank");
        links.add(documentation, starter, pros);

        FlexBoxLayout content = new FlexBoxLayout(intro, image, links);
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO);
        content.setMaxWidth("840px");
        content.setPadding(Uniform.RESPONSIVE_L);
        return content;
    }
}
