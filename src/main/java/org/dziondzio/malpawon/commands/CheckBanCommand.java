package org.dziondzio.malpawon.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.BanList;
import org.dziondzio.malpawon.Malpawon;

import java.util.List;

public class CheckBanCommand implements CommandExecutor {

    private final Malpawon plugin;

    public CheckBanCommand(Malpawon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sprawdzanie, czy komenda jest używana przez gracza
        if (!(sender instanceof Player)) {
            sender.sendMessage("Ta komenda jest dostępna tylko dla graczy.");
            return true;
        }

        // Pobranie zbanowanych graczy z API
        List<String> bannedNicks = plugin.getBannedNicks();
        StringBuilder message = new StringBuilder("Sprawdzam zbanowanych graczy:\n");

        for (String nick : bannedNicks) {
            // Sprawdzenie, czy gracz jest na serwerze
            Player player = Bukkit.getPlayer(nick.trim());
            if (player != null) {
                // Banowanie gracza
                Bukkit.getBanList(BanList.Type.NAME).addBan(nick.trim(), "Zostałeś zbanowany!", null, "Console");
                player.kickPlayer("Zostałeś zbanowany!");
                message.append("Gracz ").append(nick).append(" został zbanowany.\n");
            } else {
                message.append("Gracz ").append(nick).append(" nie jest online.\n");
            }
        }

        // Wysyłanie podsumowania do gracza wykonującego komendę
        sender.sendMessage(message.toString());
        return true;
    }
}
