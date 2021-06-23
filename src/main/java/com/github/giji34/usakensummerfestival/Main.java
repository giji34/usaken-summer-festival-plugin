package com.github.giji34.usakensummerfestival;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
  private BoatRace boatRace;

  public Main() {
  }

  @Override
  public void onEnable() {
    this.boatRace = new BoatRace(this);
    getServer().getPluginManager().registerEvents(boatRace, this);
  }
}
