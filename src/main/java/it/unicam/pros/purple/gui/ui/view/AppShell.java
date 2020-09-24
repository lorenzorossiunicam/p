package it.unicam.pros.purple.gui.ui.view;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import it.unicam.pros.purple.gui.util.Constants;

@Push(value = PushMode.MANUAL)
@PWA(name = Constants.toolName, shortName = Constants.shortName, iconPath = Constants.logo, backgroundColor = Constants.bgColor, themeColor = Constants.themeColor)
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
