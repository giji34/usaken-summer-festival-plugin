package com.github.giji34.usakensummerfestival;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerHauntedHouseSession {
  PlayerHauntedHouseSession(Player player) {
    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
  }

  void close(Player player) {
    int duration = 7 * 24 * 60 * 60 * 20;
    int amplifier = 0;
    boolean ambient = false;
    boolean particles = false;
    PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION, duration, amplifier, ambient, particles);
    player.addPotionEffect(nightVision);
  }

  void onMove(Player player) {

  }
}
