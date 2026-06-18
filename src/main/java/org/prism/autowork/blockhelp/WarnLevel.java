package org.prism.autowork.blockhelp;

public enum WarnLevel {
    CRITICAL,
    NOT_CRITICAL;


    public int getColor() {
        return switch (this) {
            case CRITICAL -> 0xf61515;
            case NOT_CRITICAL -> 0xedf619;
        };
    }
}
