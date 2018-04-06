package com.github.smk7758.TagGame;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import com.github.smk7758.TagGame.Commands.CommandExecuter;
import com.github.smk7758.TagGame.Commands.TabCompleater;
import com.github.smk7758.TagGame.Files.YamlFile;
import com.github.smk7758.TagGame.Files.YamlFileManager;
import com.github.smk7758.TagGame.Files.DataFiles.ConfigFile;
import com.github.smk7758.TagGame.Files.DataFiles.GameFile;
import com.github.smk7758.TagGame.Files.DataFiles.LanguageFile;
import com.github.smk7758.TagGame.Game.GameListener;
import com.github.smk7758.TagGame.Game.TagGame;
import com.github.smk7758.TagGame.Util.SendLog;

public class Main extends JavaPlugin {
	public static final String plugin_name = "TagGame_for_BellCocoa";
	public static boolean debug_mode = true;
	private GameListener game_listner = new GameListener(this);
	private TagGame game_manager = null;
	private Scoreboard scoreboard = null;
	public ConfigFile configfile = new ConfigFile(this);
	public GameFile gamefile = new GameFile(this);
	public LanguageFile languagefile = new LanguageFile(this);

	@Override
	public void onEnable() {
		if (!Main.plugin_name.equals(getDescription().getName())) getPluginLoader().disablePlugin(this);
		getServer().getPluginManager().registerEvents(game_listner, this);
		final PluginCommand cmd_tosogame = getCommand("TagGame");
		cmd_tosogame.setExecutor(new CommandExecuter(this));
		cmd_tosogame.setTabCompleter(new TabCompleater(this));
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard(); // better after calling plugin.
		game_manager = new TagGame(this); // have to call after scoreboard.

		saveResource(configfile, false);
		saveResource(gamefile, false);
		saveResource(languagefile, false);
		reloadFiles(); // load field to class object.
	}

	@Override
	public void onDisable() {
		if (getGameManager().isGameStarting()) {
			getGameManager().stop();
			SendLog.error("Due to plugin is disabling, the game will stop.");
		}
	}

	public void reloadFiles() {
		configfile = (ConfigFile) YamlFileManager.reloadYamlFile(configfile);
		gamefile = (GameFile) YamlFileManager.reloadYamlFile(gamefile);
		languagefile = (LanguageFile) YamlFileManager.reloadYamlFile(languagefile);
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