package it.unicam.pros.guidedsimulator.gui.ui.view.whatif;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.unicam.pros.guidedsimulator.gui.ui.view.MainLayout;

@Route(value="whatif", layout = MainLayout.class)
@PageTitle("What If Analysis | GuidedSimulator")
public class WhatIfAnalysisView extends VerticalLayout {

    public WhatIfAnalysisView(){
        H2 title = new H2("Rediscoverability");
        Text desc = new Text("THis utility permits to generate a log suitable for rediscoverability purposes...");
        add(title, desc);
    }
}
