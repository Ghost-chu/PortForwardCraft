package com.mcsunnyside.portforwardcraft;

import com.google.gson.Gson;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class Main extends JavaPlugin implements Listener {
    private List<Session> connectionPool = new ArrayList<>();
    private List<ConnectionContainer> containers = new ArrayList<>();
    private Gson gson = new Gson();
    private JSch jsch = new JSch();

    public void onEnable() {
        connectionPool.clear();
        containers.clear();
        getLogger().info("[PortForwardCraft]Plugin now is enabled!");
        saveDefaultConfig();
        reloadConfig();
        List<String> serializedData = getConfig().getStringList("data");
        for (String string : serializedData) {
            containers.add(gson.fromJson(string, ConnectionContainer.class));
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                for (ConnectionContainer container : containers) {
                    try {
                        Session session;
                        session = jsch.getSession(container.getAuser(), container.getAhost(), Integer.valueOf(container.getAport()));
                        session.setPassword(container.getApass());
                        session.setConfig("StrictHostKeyChecking", "no");
                        session.connect();

                        if (container.getMode().toLowerCase().contains("l")) {
                            getLogger().info("Creating: Mode L");
                            session.setPortForwardingL("127.0.0.1", Integer.valueOf(container.getBforward()), container.getAhost(), Integer.valueOf(container.getAforward()));
                            //session.setPortForwardingL(Integer.valueOf(container.getBforward()), "127.0.0.1", Integer.valueOf(container.getAforward()));
                        }
                        if (container.getMode().toLowerCase().contains("r")) {
                            getLogger().info("Creating: Mode R");
                            session.setPortForwardingR(Integer.valueOf(container.getBforward()), "127.0.0.1", Integer.valueOf(container.getAforward()));
                        }
                        connectionPool.add(session);

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }

            }
        }.runTaskAsynchronously(this);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Session session : connectionPool) {
                    if (!session.isConnected())
                        try {
                            session.connect();
                        } catch (Throwable th) {
                            //ignore
                        }
                }
            }
        }.runTaskTimerAsynchronously(this, 0, 200);
    }

    @Override
    public void onDisable() {
        List<String> serializedData = new ArrayList<>();
        for (ConnectionContainer container : containers) {
            serializedData.add(gson.toJson(container));
        }
        getConfig().set("data", serializedData);
        saveConfig();
        for (Session session : connectionPool) {
            session.disconnect();
        }

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 7) {
            sender.sendMessage("/pfc host user pass port Aforward Bforward mode[l/r/lr]");
            return true;
        }
        containers.add(ConnectionContainer.builder().Ahost(args[0]).Auser(args[1]).Apass(args[2]).Aport(args[3]).Aforward(args[4]).Bforward(args[5]).mode(args[6]).build());

        Bukkit.getPluginManager().disablePlugin(this);
        Bukkit.getPluginManager().enablePlugin(this);
        sender.sendMessage("Success.");
        return true;
    }
}
