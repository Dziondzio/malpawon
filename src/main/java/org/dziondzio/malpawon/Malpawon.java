package org.dziondzio.malpawon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.BanList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.dziondzio.malpawon.commands.CheckBanCommand;

public class Malpawon extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Malpawon wyganiacz włączono xd");
        getCommand("malpasprawdz").setExecutor(new CheckBanCommand(this));
        startBanChecker();
    }

    private void startBanChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTaskAsynchronously(Malpawon.this, () -> {
                    List<String> bannedNicks = getBannedNicks();

                    Bukkit.getScheduler().runTask(Malpawon.this, () -> {
                        for (String nick : bannedNicks) {
                            String trimmedNick = nick.trim();
                            getLogger().info("Checking player: " + trimmedNick);
                            Player player = Bukkit.getPlayer(trimmedNick);

                            if (player != null) {
                                if (!Bukkit.getBanList(BanList.Type.NAME).isBanned(trimmedNick)) {
                                    getLogger().info("Banning player: " + trimmedNick);
                                    Bukkit.getBanList(BanList.Type.NAME).addBan(trimmedNick, "Papa małpo!", null, "Console");
                                    player.kickPlayer("Papa małpo!");
                                } else {
                                    getLogger().info("Player " + trimmedNick + " is already banned.");
                                }
                            } else {
                                getLogger().info("Player " + trimmedNick + " is not online. Banning offline player.");
                                if (!Bukkit.getBanList(BanList.Type.NAME).isBanned(trimmedNick)) {
                                    Bukkit.getBanList(BanList.Type.NAME).addBan(trimmedNick, "Papa małpo!", null, "Console");
                                } else {
                                    getLogger().info("Player " + trimmedNick + " is already banned.");
                                }
                            }
                        }
                    });
                });
            }
        }.runTaskTimer(this, 0L, 72000L); // co godzinę sprawdza, czy małpa jest
    }

    public List<String> getBannedNicks() {
        try {
            URL url = new URL("https://malpa.zagrajnia.pl/api.php?user_id=1");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
            JsonArray nicksArray = jsonObject.getAsJsonArray("minecraft_nicks");

            List<String> bannedNicks = new ArrayList<>();
            nicksArray.forEach(element -> {
                String nickname = element.getAsJsonObject().get("nickname").getAsString();
                getLogger().info("Received nickname from API: " + nickname);
                bannedNicks.add(nickname);
            });

            return bannedNicks;

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error fetching banned nicks", e);
            return List.of();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Malpawon Pif paf");
    }
}
