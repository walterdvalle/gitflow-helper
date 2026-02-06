package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface PluginIcons {

    Icon GitFlow = IconLoader.getIcon("/icons/gitflow.svg", PluginIcons.class);
    Icon GitFlowGray = IconLoader.getIcon("/icons/gitflow_gray.svg", PluginIcons.class);
}
