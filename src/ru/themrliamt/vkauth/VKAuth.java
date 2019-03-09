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
    private Configuration configuration;

    public Configuration getConfig() {
        return this.configuration;
    }

    @Override
    public void onEnable() {
        this.loadConfig();
        MySQL sql = new MySQL(
                getConfig().getString("MySQL.Host"),
                getConfig().getString("MySQL.User"),
                getConfig().getString("MySQL.Password"),
                getConfig().getString("MySQL.Database"));
        this.getProxy().getPluginManager().registerListener(this, new AuthListener(sql, getConfig()));
        
    }   

    private void loadConfig() {
        try {
            if (!this.getDataFolder().exists()) {
                this.getDataFolder().mkdir();
            }

            File config = new File(this.getDataFolder(), "config.yml");
            if (!config.exists()) {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.getResourceAsStream("config.yml")), config);
            }

            this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

}
