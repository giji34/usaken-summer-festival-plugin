package com.github.giji34.usakensummerfestival;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
  final BoatRace boatRace;

  public Main() {
    this.boatRace = new BoatRace(this);
  }

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(boatRace, this);
  }
}
