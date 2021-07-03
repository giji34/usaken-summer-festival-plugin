package com.github.giji34.usakensummerfestival;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HauntedHouse implements Listener {
  final JavaPlugin owner;

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
      onPlayerPassingEntranceCorridor(player);
    } else if (95 <= x && x < 103 && 67 <= y && y < 69 && 25 <= z && z < 28) {
      onPlayerPassingExitCorridor(player);
    }
  }

  void onPlayerPassingEntranceCorridor(Player player) {
    if (!player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
      return;
    }
    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
  }

  void onPlayerPassingExitCorridor(Player player) {
    if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
      return;
    }
    int duration = 7 * 24 * 60 * 60 * 20;
    int amplifier = 0;
    boolean ambient = false;
    boolean particles = false;
    PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION, duration, amplifier, ambient, particles);
    player.addPotionEffect(nightVision);
  }
}
