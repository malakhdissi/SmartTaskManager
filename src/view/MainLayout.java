package view;

import controller.NavigationController;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import view.components.Sidebar;
import view.components.TopBar;

/**
 * MainLayout — standard sidebar + topbar + content shell used by every
 * "in-app" screen.
 *
 * <p>Both the sidebar and the content area are independently scrollable, so no
 * content is ever clipped on a small laptop screen, and the layout behaves on a
 * maximized window. Three screens (Welcome, Login, Deep Work) use their own
 * full-bleed layouts instead.</p>
 */
public class MainLayout extends BorderPane {

    public MainLayout(NavigationController nav, Node content, String activeKey) {
        getStyleClass().add("main-layout");

        // Sidebar — scrollable so a long nav list is never clipped.
        Sidebar sidebar = new Sidebar(nav, activeKey);
        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setFitToHeight(false);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebarScroll.getStyleClass().add("sidebar-scroll");
        setLeft(sidebarScroll);

        setTop(new TopBar(nav));

        // Content — vertically scrollable when it exceeds the window height.
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPannable(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("content-area");
        setCenter(scroll);
    }
}
