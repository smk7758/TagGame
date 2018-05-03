package com.github.smk7758.TagGame.Files.DataFiles;

import org.bukkit.plugin.Plugin;

import com.github.smk7758.TagGame.Main;
import com.github.smk7758.TagGame.Files.YamlFile;
import com.github.smk7758.TagGame.Files.YamlFileExceptField;
import com.github.smk7758.TagGame.Game.TeamManager.TeamName;
import com.github.smk7758.TagGame.Util.SendLog;
import com.github.smk7758.TagGame.Util.Utilities;

public class ConfigFile extends YamlFile {
	private final String file_name = "config.yml"; // TODO final じゃないといけない！！
	public boolean DebugMode = false;
	public Hunter Hunter;
	public Runner Runner;
	public CaughtRunner CaughtRunner;

	public class Player {
		public String Prefix, DisplayName;
		@YamlFileExceptField
		public String name; // input in loadFields()
	}

	public class Hunter extends Player {
	}

	public class Runner extends Player {
	}

	public class CaughtRunner extends Player {
	}

	public ConfigFile(Plugin plugin) {
		super(plugin);
	}

	@Override
	public String getFileName() {
		return file_name;
	}

	@Override
	public void loadField() {
		Main.debug_mode = DebugMode;
		loadPlayers();
		convertTexts();
	}

	private void convertTexts() {
		Hunter.name = Utilities.convertText(Hunter.Prefix + Hunter.DisplayName);
		Runner.name = Utilities.convertText(Runner.Prefix + Runner.DisplayName);
		CaughtRunner.name = Utilities.convertText(CaughtRunner.Prefix + CaughtRunner.DisplayName);
	}

	public void loadPlayers() {
		for (TeamName teamname : TeamName.values()) {
			Object field_object = null;
			try {
				field_object = this.getClass().getField(teamname.toString()).get(this);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
				ex.printStackTrace();
			}
			if (field_object instanceof Player) {
				Player player_object = (Player) field_object;
				teamname.prefix = player_object.Prefix;
				teamname.displayname = player_object.DisplayName;
				SendLog.debug("Player(in ConfigFile) prefix and displayname!");
			}
		}
	}

	@Override
	public void saveField() {
	}
}
