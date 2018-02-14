package ru.themrliamt.vkauth.listeners;

import com.google.common.collect.Maps;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.scheduler.BungeeScheduler;
import ru.themrliamt.vkauth.VKAuth;

import java.util.Map;
import java.util.concurrent.TimeUnit;

//Говнокод творит чудеса))
public class AuthListener implements Listener {

    private VKAuth vkAuth;
    private String AUTH_STRING;
    private String ONLOGIN_STRING;
    private String database;
    private String name;
    private Map<String, ScheduledTask> playerTasks;

    private Configuration configuration;

    public AuthListener(VKAuth vkAuth, Configuration configuration) {
        this.vkAuth = vkAuth;
        this.configuration = configuration;
        this.playerTasks = Maps.newHashMap();
        this.AUTH_STRING = configuration.getString("Strings.AuthString").replace("&", "§");
        this.ONLOGIN_STRING = configuration.getString("Strings.OnLoginString").replace("&", "§");
        this.database = configuration.getString("Settings.DatabaseName");
        this.name = configuration.getString("Settings.NameColumn");
        BungeeCord.getInstance().getPluginManager().registerListener(VKAuth.getInstance(), this);
    }

    @EventHandler
    public void onLogin(PostLoginEvent e) {
        VKAuth.getMySQL().executeQuery("SELECT COUNT(*) FROM `"+ database +"` WHERE `"+ name +"` = '"+ e.getPlayer().getName().toLowerCase() +"'", (rs) -> {

            if(!rs.next()) {
                VKAuth.getInstance().addAuth(e.getPlayer());
                return Void.TYPE;
            }

            if(rs.getInt("COUNT(*)") != 0) {

                VKAuth.getMySQL().executeQuery("SELECT * FROM `"+ database +"` WHERE `"+ name +"` = '"+ e.getPlayer().getName().toLowerCase() +"'", (r) -> {

                    if(!r.next()) {
                        VKAuth.getInstance().addAuth(e.getPlayer());
                        return Void.TYPE;
                    }

                    if(!vkAuth.isAuthed(e.getPlayer())) {
                        VKAuth.getInstance().sendCode(e.getPlayer().getName(), r.getString("VK_ID"));

                        if(r.getString("VK_ID") == null || r.getString("VK_ID").equals("")) {
                            VKAuth.getInstance().addAuth(e.getPlayer());
                            return Void.TYPE;
                        }

                        ScheduledTask task = BungeeCord.getInstance().getScheduler().schedule(vkAuth, () -> {

                            if(vkAuth.isAuthed(e.getPlayer())) {
                                return;
                            }

                            e.getPlayer().sendMessage(AUTH_STRING);
                        }, 0, 3, TimeUnit.SECONDS);
                        playerTasks.put(e.getPlayer().getName().toLowerCase(), task);
                    }

                    return Void.TYPE;
                });

            } else {
                VKAuth.getInstance().addAuth(e.getPlayer());
            }

            return Void.TYPE;
        });
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        if(!vkAuth.isAuthed(player)) {
            if(vkAuth.checkCode(player, e.getMessage())) {
                player.sendMessage(ONLOGIN_STRING);
                vkAuth.addAuth(player);
                playerTasks.get(player.getName().toLowerCase()).cancel();
            }
            e.setCancelled(true);
        }
    }

}
