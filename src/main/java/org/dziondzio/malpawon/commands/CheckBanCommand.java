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


        if (!(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
            sender.sendMessage("Ta komenda może być używana tylko z konsoli.");
            return true;
        }

        List<String> bannedNicks = plugin.getBannedNicks();
        StringBuilder message = new StringBuilder("Sprawdzam zbanowanych graczy:\n");

        for (String nick : bannedNicks) {
            String trimmedNick = nick.trim();
            Player targetPlayer = Bukkit.getPlayer(trimmedNick);


            if (!Bukkit.getBanList(BanList.Type.NAME).isBanned(trimmedNick)) {

                Bukkit.getBanList(BanList.Type.NAME).addBan(trimmedNick, "Papa małpo!", null, "Console");
                if (targetPlayer != null) {
                    targetPlayer.kickPlayer("Papa małpo!");
                    message.append("Jacek ").append(trimmedNick).append(" został zbanowany.\n");
                } else {
                    message.append("Jacek ").append(trimmedNick).append(" nie jest online, ale został zbanowany.\n");
                }
            } else {
                message.append("Jacek ").append(trimmedNick).append(" jest już zbanowany.\n");
            }
        }


        sender.sendMessage(message.toString());
        return true;
    }
}
