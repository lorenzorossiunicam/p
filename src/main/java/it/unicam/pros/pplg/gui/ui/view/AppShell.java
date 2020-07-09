package it.unicam.pros.pplg.gui.ui.view;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;

@Push(value = PushMode.MANUAL)
@PWA(name = "Guided Simulator", shortName = "G-Sim", iconPath = "images/logo.png", backgroundColor = "#233348", themeColor = "#233348")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
public class AppShell implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addMetaTag("apple-mobile-web-app-capable", "yes");
        settings.addMetaTag("apple-mobile-web-app-status-bar-style", "black");
        settings.addFavIcon("icon", "frontend/images/favicons/favicon.ico",
                "256x256");
    }
}
