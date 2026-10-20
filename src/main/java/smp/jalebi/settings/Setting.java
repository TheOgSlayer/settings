package smp.jalebi.settings;

public enum Setting {
    TPA("tpa"),
    TPAHERE("tpahere"),
    PAYMENTS("payments"),
    PUBLIC_CHAT("public-chat"),
    PRIVATE_MESSAGES("private-messages");

    public final String path;

    Setting(String path) {
        this.path = path;
    }
}
