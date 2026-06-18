package org.prism.autowork.blockhelp;

public interface WarningProvider {
    WarnLevel warnLevel();
    String getWarning();
}
