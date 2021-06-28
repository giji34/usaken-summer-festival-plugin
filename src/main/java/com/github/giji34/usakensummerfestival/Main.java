package com.github.giji34.usakensummerfestival;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
  private BoatRace boatRace;
  private BowShooting bowShooting;

  public Main() {
  }

  @Override
  public void onEnable() {
    this.boatRace = new BoatRace(this);
    this.bowShooting = new BowShooting(this);
    PluginManager pluginManager = getServer().getPluginManager();
    pluginManager.registerEvents(boatRace, this);
    pluginManager.registerEvents(bowShooting, this);
  }
}
