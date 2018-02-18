package ru.themrliamt.vkauth;

import com.google.common.collect.Maps;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import ru.themrliamt.vkauth.database.MySQL;
import ru.themrliamt.vkauth.listeners.AuthListener;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class VKAuth extends Plugin {

    private static VKAuth instance;

    private static VKUtils vkUtils;
    private static Map<String, String> authorizedPlayers;
    private static Map<String, String> codes;
    private static MySQL mySQL;
    private Configuration configuration;

    public static VKAuth getInstance() {
        return instance;
    }

    public static MySQL getMySQL() {
        return mySQL;
    }

    public Configuration getConfig() {
        return this.configuration;
    }

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        vkUtils = new VKUtils(getConfig().getString("Settings.VKToken"));
        authorizedPlayers = Maps.newHashMap();
        codes = Maps.newHashMap();
        mySQL = new MySQL(
                getConfig().getString("MySQL.Host"),
                getConfig().getString("MySQL.User"),
                getConfig().getString("MySQL.Password"),
                getConfig().getString("MySQL.Database"));
        new AuthListener(this, getConfig());
    }

    public boolean isAuthed(ProxiedPlayer player) {
        return authorizedPlayers
                .containsKey(player.getName().toLowerCase())
                && authorizedPlayers.get(player.getName().toLowerCase())
                .equals(player.getAddress().getHostName());
    }

    public boolean checkCode(ProxiedPlayer player, String code) {
        return codes
                .containsKey(player.getName().toLowerCase())
                && codes.get(player.getName().toLowerCase())
                .contains(code);
    }

    public void sendCode(String player, String user) {
        String randomCode = Long.toHexString(Double.doubleToLongBits(Math.random()));

        vkUtils.sendCode(randomCode, user);
        codes.put(player.toLowerCase(), randomCode);
    }

    public void addAuth(ProxiedPlayer player) {
        authorizedPlayers.put(
                player.getName().toLowerCase(),
                player.getAddress().getHostName());
    }

    private void loadConfig() {
        try {
            if (!this.getDataFolder().exists()) {
                this.getDataFolder().mkdir();
            }

            File e = new File(this.getDataFolder(), "config.yml");
            if (!e.exists()) {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.getResourceAsStream("config.yml")), e);
            }

            this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(e);
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

}
