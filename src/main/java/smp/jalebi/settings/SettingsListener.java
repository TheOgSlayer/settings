package smp.jalebi.settings;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

public class SettingsListener implements Listener {
    private static final Set<String> TPA = Set.of("tpa", "tpask", "call", "etpa", "etpask", "ecall");
    private static final Set<String> TPAHERE = Set.of("tpahere", "etpahere");
    private static final Set<String> PAY = Set.of("pay", "epay");
    private static final Set<String> MSG = Set.of("msg", "tell", "w", "whisper", "m", "t", "pm",
            "emsg", "etell", "ewhisper", "dm");

    private final JalebiSettings plugin;

    public SettingsListener(JalebiSettings plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------ commands

    @EventHandler(priority = EventPriority.LOW)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String raw = e.getMessage();
        if (raw.length() < 2) return;
        String[] parts = raw.substring(1).trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return;

        String label = parts[0].toLowerCase(Locale.ROOT);
        String base = label.contains(":") ? label.substring(label.indexOf(':') + 1) : label;
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
        Player sender = e.getPlayer();

        // every variant of /kill (minecraft:kill, essentials:kill, ...) is routed to our own handler
        if (base.equals("kill")) {
            e.setCancelled(true);
            plugin.handleKill(sender, args);
            return;
        }

        if (args.length == 0) return;

        Setting setting;
        String blockedMessage;
        if (TPA.contains(base)) {
            setting = Setting.TPA;
            blockedMessage = "that player is not accepting tpa requests";
        } else if (TPAHERE.contains(base)) {
            setting = Setting.TPAHERE;
            blockedMessage = "that player is not accepting tpahere requests";
        } else if (PAY.contains(base)) {
            setting = Setting.PAYMENTS;
            blockedMessage = "that player is not accepting payments";
        } else if (MSG.contains(base)) {
            setting = Setting.PRIVATE_MESSAGES;
            blockedMessage = "that player has private messages turned off";
        } else {
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) target = Bukkit.getPlayer(args[0]);
        if (target == null || target.equals(sender)) return;

        if (!plugin.settings().get(target.getUniqueId(), setting)) {
            e.setCancelled(true);
            sender.sendMessage(Msg.error(blockedMessage));
        }
    }

    // ------------------------------------------------------------------ public chat

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncChatEvent e) {
        Player sender = e.getPlayer();
        if (!plugin.settings().get(sender.getUniqueId(), Setting.PUBLIC_CHAT)) {
            e.setCancelled(true);
            sender.sendMessage(Msg.error("your public chat is turned off, enable it in /settings"));
            return;
        }
        e.viewers().removeIf(a -> a instanceof Player p
                && !plugin.settings().get(p.getUniqueId(), Setting.PUBLIC_CHAT));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.forgetKill(e.getPlayer().getUniqueId());
    }
}
