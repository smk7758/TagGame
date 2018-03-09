package com.github.smk7758.TagGame.Files.DataFiles;

import org.bukkit.plugin.Plugin;

import com.github.smk7758.TagGame.Main;
import com.github.smk7758.TagGame.Files.YamlFile;
import com.github.smk7758.TagGame.Game.ScorebordTeam.TeamName;
import com.github.smk7758.TagGame.Util.SendLog;

public class ConfigFile extends YamlFile {
	private final String file_name = "config.yml"; // TODO final じゃないといけない！！
	public boolean DebugMode = false;
	public Hunter Hunter;
	public Runner Runner;
	public RunnerPrisoner RunnerPrisoner;
	public OtherPlayer OtherPlayer;

	public class Player {
		public String Prefix, DisplayName;
	}

	public class Hunter extends Player {
	}

	public class Runner extends Player {
	}

	public class RunnerPrisoner extends Player {
	}

	public class OtherPlayer extends Player {
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
		testPlayers();
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
		// try {
		// for (TeamName teamname : TeamName.values()) {
		// SendLog.debug("TeamName: " + teamname.toString());
		// Field field_class = this.getClass().getField(teamname.toString());
		// SendLog.debug("ClassName: " + field_class.getName());
		// for (Field field_class_field : field_class.getClass().getFields()) {
		// SendLog.debug("ClassField: " + field_class_field.getName());
		// if (Modifier.isFinal(field_class_field.getModifiers())) {
		// SendLog.debug("is final!");
		// continue;
		// } else {
		// SendLog.debug("not final!");
		// teamname.getClass().getField(field_class_field.getName()).set(this,
		// field_class_field.get(field_class));
		// }
		// }
		// }
		// } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
		// ex.printStackTrace();
		// }
		// for (TeamName teamname : TeamName.values()) {
		// Field field_class = this.getClass().getField(teamname.toString());
		// SendLog.debug("ClassName: " + field_class.getName());
		// for (Field field_teamname : teamname.getClass().getFields()) {
		// SendLog.debug("TeamNameField: " + field_teamname.getName());
		// Field field_class_field = field_class.getClass().getField(field_teamname.getName());
		// SendLog.debug("ClassField: " + field_class_field.getName());
		// field_teamname.set(teamname, field_class_field.get(field_class));
		// }
		// }
	}

	public void testPlayers() {
		SendLog.debug("TEST Players in ConfigFile.");
		SendLog.debug(TeamName.Hunter.displayname);
		SendLog.debug(TeamName.Hunter.prefix);
	}

	@Override
	public void saveField() {
	}
}
