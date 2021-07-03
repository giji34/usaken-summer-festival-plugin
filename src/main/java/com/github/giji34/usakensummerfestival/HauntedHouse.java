package com.github.giji34.usakensummerfestival;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class HauntedHouse implements Listener {
  final JavaPlugin owner;
  final HashMap<UUID, PlayerHauntedHouseSession> sessions = new HashMap<>();

  HauntedHouse(JavaPlugin owner) {
    this.owner = owner;
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent e) {
    Player player = e.getPlayer();
    Location location = player.getLocation();
    World world = location.getWorld();
    if (world == null) {
      return;
    }
    if (world.getEnvironment() != World.Environment.NORMAL) {
      return;
    }
    double x = location.getX();
    double y = location.getY();
    double z = location.getZ();
    if (104 <= x && x < 115 && 67 <= y && y < 69 && 20 <= z && z < 23) {
      if (!sessions.containsKey(player.getUniqueId())) {
        PlayerHauntedHouseSession session = new PlayerHauntedHouseSession(player);
        sessions.put(player.getUniqueId(), session);
      }
    } else if (95 <= x && x < 103 && 67 <= y && y < 69 && 25 <= z && z < 28) {
      if (sessions.containsKey(player.getUniqueId())) {
        PlayerHauntedHouseSession session = sessions.get(player.getUniqueId());
        sessions.remove(player.getUniqueId());
        session.close(player);
      }
    } else if (sessions.containsKey(player.getUniqueId())) {
      PlayerHauntedHouseSession session = sessions.get(player.getUniqueId());
      session.onMove(player);
    }
  }
}
