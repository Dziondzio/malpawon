package org.dziondzio.malpawon;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.BanList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.dziondzio.malpawon.commands.CheckBanCommand;

public class Malpawon extends JavaPlugin implements Listener {

    private Set<String> blockedIPs = new HashSet<>();
    private Logger logger;
    private long lastFetchTime = 0;
    private List<String> cachedBannedNicks = new ArrayList<>();
    private static final long FETCH_INTERVAL = 300000; // 5 minut

    @Override
    public void onEnable() {
        saveDefaultConfig();
        logger = getLogger();
        logger.info("Malpawon wyganiacz włączono xd");

        blockedIPs.addAll(getConfig().getStringList("blocked-ips"));
        logger.info("Zablokowane IP: " + blockedIPs.toString());

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("malpasprawdz").setExecutor(new CheckBanCommand(this));
        startBanChecker();
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerIP = event.getAddress().getHostAddress();

        if (blockedIPs.contains(playerIP)) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Papa małpo!");
            logger.info("Jacek o IP " + playerIP + " został wyrzucony z serwera.");
        }
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
                            logger.info("Sprawdzanie jacka: " + trimmedNick);
                            Player player = Bukkit.getPlayer(trimmedNick);

                            if (player != null) {
                                if (!Bukkit.getBanList(BanList.Type.NAME).isBanned(trimmedNick)) {
                                    logger.info("Banowanie jacka: " + trimmedNick);
                                    Bukkit.getBanList(BanList.Type.NAME).addBan(trimmedNick, "Papa małpo!", null, "Console");
                                    player.kickPlayer("Papa małpo!");
                                } else {
                                    logger.info("Jacek " + trimmedNick + " jest zbanowany");
                                }
                            } else {
                                logger.info("Jacek " + trimmedNick + " nie był tu jeszcze, ale zbanujemy go offline");
                                if (!Bukkit.getBanList(BanList.Type.NAME).isBanned(trimmedNick)) {
                                    Bukkit.getBanList(BanList.Type.NAME).addBan(trimmedNick, "Papa małpo!", null, "Console");
                                } else {
                                    logger.info("Jacek " + trimmedNick + " jest już zbanowany");
                                }
                            }
                        }
                    });
                });
            }
        }.runTaskTimer(this, 0L, 72000L); // co godzinę sprawdza, czy małpa jest
    }

    public List<String> getBannedNicks() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFetchTime < FETCH_INTERVAL) {
            return cachedBannedNicks;
        }

        try {
            URL url = new URL("https://widzowiemalpy.pl/api/user/minecraft-nick");
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

            JsonArray nicksArray = gson.fromJson(response.toString(), JsonArray.class);

            cachedBannedNicks.clear();
            nicksArray.forEach(element -> {
                String nickname = element.getAsJsonObject().get("nickname").getAsString();
                logger.info("Jacka niciki z api: " + nickname);
                cachedBannedNicks.add(nickname);
            });

            lastFetchTime = currentTime;
            return cachedBannedNicks;

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Network error while fetching banned nicks", e);
            return List.of();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing API response", e);
            return List.of();
        }
    }

    @Override
    public void onDisable() {
        logger.info("Malpawon Pif paf");
    }
}
