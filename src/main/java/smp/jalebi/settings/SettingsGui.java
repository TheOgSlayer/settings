package smp.jalebi.settings;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SettingsGui {
    private static final ClickCallback.Options OPTS =
            ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build();
    private static final int WIDTH = 220;

    private final JalebiSettings plugin;

    public SettingsGui(JalebiSettings plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------ helpers

    private ActionButton btn(Component label, Component tooltip, Consumer<Player> onClick) {
        return ActionButton.create(label, tooltip, WIDTH, DialogAction.customClick((view, audience) -> {
            if (audience instanceof Player p) Bukkit.getScheduler().runTask(plugin, () -> onClick.accept(p));
        }, OPTS));
    }

    private Dialog dialog(Component title, List<DialogBody> body, DialogType type) {
        return Dialog.create(f -> f.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(body)
                        .build())
                .type(type));
    }

    /** icon from config: "text <chars>" or "sprite <atlas> <sprite>" */
    private Component icon(String key, String def, TextColor color) {
        String v = plugin.getConfig().getString("icons." + key, def).trim();
        if (v.startsWith("sprite ")) {
            String[] p = v.substring(7).trim().split("\\s+");
            if (p.length == 2) {
                return Component.object(ObjectContents.sprite(k(p[0]), k(p[1]))).color(color);
            }
        }
        if (v.startsWith("text ")) v = v.substring(5);
        return Component.text(v, color);
    }

    private static Key k(String s) {
        return s.contains(":") ? Key.key(s) : Key.key("minecraft", s);
    }

    private Component toggleLabel(String name, boolean value, String onText, String offText) {
        return Component.text(name + ": ", NamedTextColor.WHITE)
                .append(Component.text(value ? onText : offText,
                        value ? NamedTextColor.GREEN : NamedTextColor.RED));
    }

    private Component clickToToggle() {
        return Msg.sc("click to toggle", NamedTextColor.GRAY);
    }

    private List<DialogBody> subtitle(String text) {
        return List.of(DialogBody.plainMessage(Component.text(text, NamedTextColor.GRAY)));
    }

    // ------------------------------------------------------------------ main page

    public void openMain(Player p) {
        List<ActionButton> actions = new ArrayList<>();
        actions.add(btn(icon("privacy", "text \uD83D\uDD12", NamedTextColor.RED)
                        .append(Component.text(" Privacy", NamedTextColor.WHITE)),
                null, this::openPrivacy));
        actions.add(btn(icon("chat", "sprite items item/paper", NamedTextColor.WHITE)
                        .append(Component.text(" Chat", NamedTextColor.WHITE)),
                null, this::openChat));

        p.showDialog(dialog(Component.text("Settings"),
                subtitle("Choose a category to change your Jalebi SMP settings"),
                DialogType.multiAction(actions, null, 1)));
    }

    // ------------------------------------------------------------------ privacy

    public void openPrivacy(Player p) {
        List<ActionButton> actions = new ArrayList<>();
        actions.add(toggle(p, Setting.TPA, "TPA REQUESTS", "ANYONE", "NO ONE", this::openPrivacy));
        actions.add(toggle(p, Setting.TPAHERE, "TPAHERE REQUESTS", "ANYONE", "NO ONE", this::openPrivacy));
        actions.add(toggle(p, Setting.PAYMENTS, "ALLOW PAYMENTS", "ANYONE", "NO ONE", this::openPrivacy));
        ActionButton back = btn(Component.text("Back"), null, this::openMain);

        p.showDialog(dialog(Component.text("Privacy"),
                subtitle("Click an option to toggle it"),
                DialogType.multiAction(actions, back, 1)));
    }

    // ------------------------------------------------------------------ chat

    public void openChat(Player p) {
        List<ActionButton> actions = new ArrayList<>();
        actions.add(toggle(p, Setting.PUBLIC_CHAT, "Public Chat", "ON", "OFF", this::openChat));
        actions.add(toggle(p, Setting.PRIVATE_MESSAGES, "Private Messages (/msg)", "ON", "OFF", this::openChat));
        ActionButton back = btn(Component.text("Back"), null, this::openMain);

        p.showDialog(dialog(Component.text("Chat"),
                subtitle("Click an option to toggle it"),
                DialogType.multiAction(actions, back, 1)));
    }

    private ActionButton toggle(Player p, Setting s, String name, String on, String off,
                                Consumer<Player> reopen) {
        boolean value = plugin.settings().get(p.getUniqueId(), s);
        return btn(toggleLabel(name, value, on, off), clickToToggle(), pl -> {
            plugin.settings().toggle(pl.getUniqueId(), s);
            reopen.accept(pl);
        });
    }
}
