package com.github.smk7758.TagGame;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import com.github.smk7758.TagGame.Files.YamlFile;
import com.github.smk7758.TagGame.Files.YamlFileManager;
import com.github.smk7758.TagGame.Files.DataFiles.ConfigFile;
import com.github.smk7758.TagGame.Files.DataFiles.GameFile;
import com.github.smk7758.TagGame.Files.DataFiles.LanguageFile;
import com.github.smk7758.TagGame.Game.GameListener;
import com.github.smk7758.TagGame.Game.TagGame;

public class Main extends JavaPlugin {
	public static final String plugin_name = "TagGame_for_BellCocoa";
	public static boolean debug_mode = true;
	private GameListener game_listner = new GameListener(this);
	private TagGame game_manager = null;
	private Scoreboard scoreboard = null;
	private YamlFileManager yfm = null;
	public ConfigFile configfile = null;
	public GameFile gamefile = null;
	public LanguageFile languagefile = null;

	@Override
	public void onEnable() {
		if (!Main.plugin_name.equals(getDescription().getName())) getPluginLoader().disablePlugin(this);
		getServer().getPluginManager().registerEvents(game_listner, this);
		final PluginCommand cmd_tosogame = getCommand("TagGame");
		cmd_tosogame.setExecutor(new CommandExecuter(this));
		cmd_tosogame.setTabCompleter(new TabCompleater(this));
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		configfile = new ConfigFile(this);
		saveResource(configfile, false);
		gamefile = new GameFile(this);
		saveResource(gamefile, false);
		languagefile = new LanguageFile(this);
		saveResource(languagefile, false);
		reloadFiles(); // load field to class object.

		game_manager = new TagGame(this);
	}

	@Override
	public void onDisable() {
	}

	public void reloadFiles() {
		configfile = (ConfigFile) YamlFileManager.reloadYamlFile(configfile);
		gamefile = (GameFile) YamlFileManager.reloadYamlFile(gamefile);
		languagefile = (LanguageFile) YamlFileManager.reloadYamlFile(languagefile);
	}

	public YamlFileManager getYamlFileManager() {
		return yfm;
	}

	public TagGame getGameManager() {
		return game_manager;
	}

	public Scoreboard getScoreBoard() {
		return scoreboard;
	}

	public void saveResource(YamlFile file, boolean replace) {
		if (replace || !file.getFile().exists()) saveResource(file.getFileName(), replace);
	}
	// TODO: store inv, exp
	// TODO: book or chat, recalling?reusing? the game - need?
	// DONE: config
}