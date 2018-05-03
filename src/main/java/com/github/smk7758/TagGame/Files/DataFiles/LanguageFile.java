package com.github.smk7758.TagGame.Files.DataFiles;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.github.smk7758.TagGame.Files.YamlFile;
import com.github.smk7758.TagGame.Files.YamlFileExceptField;
import com.github.smk7758.TagGame.Util.SendLog;
import com.github.smk7758.TagGame.Util.Utilities;

public class LanguageFile extends YamlFile {
	private final String file_name = "language.yml";
	public String startToRunner, startToHunter, stop, finishByTimeToRunner, finishByTimeToHunter,
			finishByCaughtToRunner, finishByCaughtToHunter, catchRunnerToPlayer, catchRunnerWaitTeleportToPlayer,
			catchRunnerToOthers, outRunnerToOthers, outRunnerToPlayer, lessPlayersOfAdventure,
			lessCommandArguments, mustSendCommandFromPlayer, saveCommand, reloadCommand, startCommand,
			startCommandError,
			stopCommand, stopCommandError, setTeam, setTeamError, removeTeam, removeTeamError, setLocation,
			startCheckNotSetted, startCheckNoPlayers, setToHunterOnStart, setToRunnerOnStart;
	@YamlFileExceptField
	public Map<String, String> argumentsTop = new HashMap<>(), argumentsPlayerTypes = new HashMap<>(),
			arugmentsLocations = new HashMap<>();

	public LanguageFile(Plugin plugin) {
		super(plugin);
	}

	@Override
	public String getFileName() {
		return file_name;
	}

	@Override
	public void loadField() {
		convertTexts();
		for (String path_child : getFileConfiguration().getConfigurationSection("argumentsTop").getKeys(false)) {
			argumentsTop.put(path_child,
					getFileConfiguration().getString("argumentsTop" + "." + path_child));
		}
		for (String path_child : getFileConfiguration().getConfigurationSection("argumentsPlayerTypes").getKeys(false)) {
			argumentsPlayerTypes.put(path_child,
					getFileConfiguration().getString("argumentsPlayerTypes" + "." + path_child));
		}
		for (String path_child : getFileConfiguration().getConfigurationSection("arugmentsLocations").getKeys(false)) {
			arugmentsLocations.put(path_child,
					getFileConfiguration().getString("arugmentsLocations" + "." + path_child));
		}
	}

	@Override
	public void saveField() {
	}

	private void convertTexts() {
		for (Field field : this.getClass().getFields()) {
			if (field.getType().equals(String.class)) {
				try {
					String text = (String) field.get(this);
					if (text != null && !text.isEmpty()) {
						text = Utilities.convertText(text);
						field.set(this, text);
					} else {
						SendLog.debug("Field: " + field.getName() + " is null.");
					}
				} catch (IllegalArgumentException | IllegalAccessException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public String convertText(String text, CommandSender sender, int wait_time) {
		return Utilities.convertText(text, sender).replaceAll("%wait_time%", String.valueOf(wait_time));
	}
}
