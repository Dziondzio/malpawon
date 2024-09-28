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
        getLogger().info("Malpawon plugin enabled");
        getCommand("malpasprawdz").setExecutor(new CheckBanCommand(this)); // Rejestracja komendy
        startBanChecker();
    }


    private void startBanChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Run network operation asynchronously
                Bukkit.getScheduler().runTaskAsynchronously(Malpawon.this, () -> {
                    List<String> bannedNicks = getBannedNicks();

                    // Back to the main thread to modify player state
                    Bukkit.getScheduler().runTask(Malpawon.this, () -> {
                        for (String nick : bannedNicks) {
                            // Trim any spaces from nicknames
                            String trimmedNick = nick.trim();
                            getLogger().info("Checking player: " + trimmedNick);

                            Player player = Bukkit.getPlayer(trimmedNick);

                            if (player != null) {
                                getLogger().info("Banning player: " + trimmedNick);

                                // Ban the player using the NAME ban list
                                Bukkit.getBanList(BanList.Type.NAME).addBan(trimmedNick, "Zostałeś zbanowany!", null, "Console");

                                // Kick the player after banning
                                player.kickPlayer("Zostałeś zbanowany!");

                            } else {
                                getLogger().info("Player " + trimmedNick + " is not online.");
                            }
                        }
                    });
                });
            }
        }.runTaskTimer(this, 0L, 12000L); // 12000L = 10 minutes
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

            // Parse JSON with Gson
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
            return List.of(); // Return an empty list in case of error
        }
    }

    // Metoda do sprawdzania, czy gracz jest zbanowany
    public boolean isPlayerBanned(String nickname) {
        List<String> bannedNicks = getBannedNicks(); // Możesz wykorzystać tę samą metodę
        return bannedNicks.contains(nickname);
    }

    @Override
    public void onDisable() {
        getLogger().info("Malpawon plugin disabled");
    }
}
