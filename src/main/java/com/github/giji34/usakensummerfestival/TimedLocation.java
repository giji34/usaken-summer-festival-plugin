package com.github.giji34.usakensummerfestival;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

class TimedLocation {
  final long timeMillis;
  final Vector location;

  TimedLocation(Player player) {
    this.timeMillis = System.currentTimeMillis();
    Location l = player.getLocation();
    this.location = l.toVector();
  }
}
