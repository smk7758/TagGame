package com.github.smk7758.TagGame.Files.DataFiles;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.github.smk7758.TagGame.Files.YamlFile;
import com.github.smk7758.TagGame.Files.YamlFileExceptField;
import com.github.smk7758.TagGame.Game.TeamManager.TeamName;
import com.github.smk7758.TagGame.Util.SendLog;
import com.github.smk7758.TagGame.Util.Utilities;

public class GameFile extends YamlFile {
	private final String file_name = "game.yml"; // TODO final じゃないといけない！！
	public String GameName;
	public int TeleportWaitTime;
	@YamlFileExceptField
	public Time GameLength;
	@YamlFileExceptField
	public Map<String, TeamName> Players; // TODO
	@YamlFileExceptField
	public Location lobby_loc, spawn_loc, respawn_loc;
	public RunnerItems RunnerItems;

	public class RunnerItems {
		public Feather Feather;
		public Bone Bone;

		public class Feather {
			public String Name;
			public List<String> Lore;
		}

		public class Bone {
			public String Name;
			public List<String> Lore;
		}
	}

	public class Time {
		public int minute = 0;

		public Time(int minute) {
			this.minute = minute;
		}

		public int getAsSecond() {
			return minute * 60;
		}
	}

	public GameFile(Plugin plugin) {
		super(plugin);
	}

	@Override
	public String getFileName() {
		return file_name;
	}

	@Override
	public void loadField() {
		reloadFileConfiguration(); // TODO これがなぜか必要。Why!?
		SendLog.debug("loadField Method!");
		GameLength = new Time(getFileConfiguration().getInt("GameLength"));

		lobby_loc = getLocation("Lobby.Location");
		spawn_loc = getLocation("Spawn.Location");
		respawn_loc = getLocation("Respawn.Location");

		convertTexts();
	}

	private void convertTexts() {
		RunnerItems.Feather.Name = Utilities.convertText(RunnerItems.Feather.Name);
		for (int i = 0; i > RunnerItems.Feather.Lore.size(); i++) {
			RunnerItems.Feather.Lore.set(i, Utilities.convertText(RunnerItems.Feather.Lore.get(i)));
		}
		RunnerItems.Bone.Name = Utilities.convertText(RunnerItems.Bone.Name);
		for (int i = 0; i > RunnerItems.Bone.Lore.size(); i++) {
			RunnerItems.Bone.Lore.set(i, Utilities.convertText(RunnerItems.Bone.Lore.get(i)));
		}
	}

	public Location getLocation(String path) {
		String world_name = getFileConfiguration().getString(path + ".World");
		World world = null;
		if (world_name == null || (world = Bukkit.getServer().getWorld(world_name)) == null) {
			SendLog.error("Can't find: " + path + ".World");
			world = Bukkit.getWorlds().get(0);
		}
		double x = getFileConfiguration().getDouble(path + ".X");
		double y = getFileConfiguration().getDouble(path + ".Y");
		double z = getFileConfiguration().getDouble(path + ".Z");
		return new Location(world, x, y, z);
	}

	@Override
	public void saveField() {
		saveLocaton(lobby_loc, "Lobby.Location");
		saveLocaton(spawn_loc, "Spawn.Location");
		saveLocaton(respawn_loc, "Respawn.Location");
	}

	private void saveLocaton(Location loc, String path) {
		if (loc == null) return;
		getFileConfiguration().set(path + ".World", loc.getWorld().getName());
		getFileConfiguration().set(path + ".X", loc.getX());
		getFileConfiguration().set(path + ".Y", loc.getY());
		getFileConfiguration().set(path + ".Z", loc.getZ());
	}
}
