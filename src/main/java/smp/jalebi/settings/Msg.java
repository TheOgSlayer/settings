package smp.jalebi.settings;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/** Every chat message goes through here so the text is always small caps. */
public final class Msg {
    public static final TextColor BLUE = TextColor.fromHexString("#00a2ff");

    private Msg() {}

    public static Component sc(String text, TextColor color) {
        return Component.text(SmallCaps.of(text), color);
    }

    public static Component error(String text) {
        return sc(text, NamedTextColor.RED);
    }
}
