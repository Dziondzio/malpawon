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

        if (!(sender instanceof Player)) {
            sender.sendMessage("Ta komenda jest dostępna tylko dla graczy.");
            return true;
        }

        Player player = (Player) sender;


        if (!player.hasPermission("malpawon.ban")) {
            player.sendMessage("Nie masz uprawnień do używania tej komendy.");
            return true;
        }


        List<String> bannedNicks = plugin.getBannedNicks();
        StringBuilder message = new StringBuilder("Sprawdzam zbanowanych graczy:\n");

        for (String nick : bannedNicks) {
            String trimmedNick = nick.trim();
            Player targetPlayer = Bukkit.getPlayer(trimmedNick);


            Bukkit.getBanList(BanList.Type.NAME).addBan(trimmedNick, "Papa małpo!", null, "Console");

            if (targetPlayer != null) {
                targetPlayer.kickPlayer("Papa małpo!");
                message.append("Gracz ").append(trimmedNick).append(" został zbanowany.\n");
            } else {
                message.append("Gracz ").append(trimmedNick).append(" nie jest online, ale został zbanowany.\n");
            }
        }


        player.sendMessage(message.toString());
        return true;
    }
}
