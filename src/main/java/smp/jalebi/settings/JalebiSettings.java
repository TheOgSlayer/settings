package smp.jalebi.settings;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class JalebiSettings extends JavaPlugin {
    private SettingsManager settings;
    private SettingsGui gui;
    private final Map<UUID, Long> killPending = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = new SettingsManager(this);
        gui = new SettingsGui(this);
        getServer().getPluginManager().registerEvents(new SettingsListener(this), this);
    }

    public SettingsManager settings() { return settings; }
    public SettingsGui gui() { return gui; }

    // ------------------------------------------------------------------ commands

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (cmd.getName().toLowerCase(Locale.ROOT)) {
            case "settings" -> {
                if (sender instanceof Player p) gui.openMain(p);
                else sender.sendMessage("Players only.");
            }
            case "ping" -> ping(sender, args);
            case "kill" -> {
                if (sender instanceof Player p) handleKill(p, args);
                else sender.sendMessage("Players only.");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ping") && args.length == 1) {
            List<String> out = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                    out.add(p.getName());
            }
            return out;
        }
        return List.of(); // /kill and /settings never suggest names
    }

    private void ping(CommandSender sender, String[] args) {
        Player target;
        if (args.length > 0) {
            target = Bukkit.getPlayerExact(args[0]);
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(Msg.error("usage: /ping <player>"));
            return;
        }
        if (target == null) {
            sender.sendMessage(Msg.error("that player is not online"));
            return;
        }
        sender.sendMessage(Component.text(target.getName(), NamedTextColor.WHITE)
                .append(Msg.sc("'s ping is ", NamedTextColor.WHITE))
                .append(Component.text(String.valueOf(target.getPing()), Msg.BLUE))
                .append(Msg.sc(" ms", NamedTextColor.WHITE)));
    }

    /** /kill only ever kills yourself, and has to be typed twice. */
    public void handleKill(Player p, String[] args) {
        UUID id = p.getUniqueId();
        if (args.length > 0) {
            killPending.remove(id);
            p.sendMessage(Msg.error("you can only use /kill on yourself, just type /kill"));
            return;
        }
        long window = Math.max(1, getConfig().getInt("kill-confirm-seconds", 10)) * 1000L;
        Long first = killPending.get(id);
        long now = System.currentTimeMillis();
        if (first != null && now - first <= window) {
            killPending.remove(id);
            p.setHealth(0.0);
        } else {
            killPending.put(id, now);
            p.sendMessage(Msg.sc("are you sure? type ", NamedTextColor.RED)
                    .append(Msg.sc("/kill", NamedTextColor.WHITE))
                    .append(Msg.sc(" again to confirm", NamedTextColor.RED)));
        }
    }

    public void forgetKill(UUID id) {
        killPending.remove(id);
    }
}
