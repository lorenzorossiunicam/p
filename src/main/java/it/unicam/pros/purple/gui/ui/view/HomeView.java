package it.unicam.pros.purple.gui.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.unicam.pros.purple.gui.ui.components.FlexBoxLayout;
import it.unicam.pros.purple.gui.ui.layout.size.Horizontal;
import it.unicam.pros.purple.gui.ui.layout.size.Uniform;
import it.unicam.pros.purple.gui.ui.util.UIUtils;
import it.unicam.pros.purple.gui.util.Constants;

@Route(value="", layout = MainLayout.class)
@PageTitle("Home | "+ Constants.shortName)
public class HomeView extends ViewFrame {

    public HomeView(){
        setId("Home");
        setViewContent(createContent());
    }

    private Component createContent() {
        Html intro = new Html("<p>PURPLE (PURpos Parametric Log gEnerator) is a progressive web app written in" +
                " Java using the development framework <a href='https://www.vaadin.com' target='_blank'>Vaadin</a>.  " +
                "This permits to use PURPLE from any operating system via web browser." +
                "PURPLE generates event log tuned for a specific mining-purpose by simulating a business model." +
                "For further information please check the links below.</p>");

        Div image = new Div();
        Image img = new Image(UIUtils.IMG_PATH + "arch.png", "purple_arch");

        img.setWidth("100%");
        img.setHeight("auto");
        image.add(img);
        image.getStyle().set("padding", "30px 0px 50px 0px");

        Row links = new Row();
        Button b1 =  UIUtils.createButton("Get the source code", VaadinIcon.EXTERNAL_LINK);
        b1.setWidth("100%");
        Button b2 = UIUtils.createButton("ProsLab Team", VaadinIcon.EXTERNAL_LINK);
        b2.setWidth("100%");
        //Anchor documentation = new Anchor("https://bitbucket.org/proslabteam/guidedsimulator/src/master/README.md", UIUtils.createButton("Read the documentation", VaadinIcon.EXTERNAL_LINK));
        Anchor starter = new Anchor("https://bitbucket.org/proslabteam/purple/src", b1);
        Anchor pros = new Anchor("http://pros.unicam.it/", b2);

        //documentation.setTarget("_blank");
        starter.setTarget("_blank");
        pros.setTarget("_blank");
        links.add(starter, pros);

        FlexBoxLayout content = new FlexBoxLayout(new H1("Welcome to PURPLE!"),intro, image, links);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO);
        content.setMaxWidth("840px");
        content.setPadding(Uniform.RESPONSIVE_L);
        return content;
    }
}
