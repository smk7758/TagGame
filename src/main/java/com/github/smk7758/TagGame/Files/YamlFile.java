package com.github.smk7758.TagGame.Files;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public abstract class YamlFile {
	private FileConfiguration config = null;
	private File file = null;

	public YamlFile(Plugin plugin) {
		if (getFileName() == null) throw new NullPointerException("!!!! file name is null.");
		file = new File(plugin.getDataFolder(), getFileName());
		reloadFileConfiguration();
	}

	/**
	 * @return with file extension: .yml
	 */
	public abstract String getFileName();

	public abstract void loadField();

	public abstract void saveField();

	public FileConfiguration getFileConfiguration() {
		return config != null ? config : reloadFileConfiguration();
	}

	public FileConfiguration reloadFileConfiguration() {
		return (config = YamlConfiguration.loadConfiguration(file));
	}

	public File getFile() {
		return file;
	}
}
