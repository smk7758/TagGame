package com.github.smk7758.TagGame.Game;

import java.io.Closeable;
import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.github.smk7758.TagGame.Main;
import com.github.smk7758.TagGame.Util.SendLog;

import net.md_5.bungee.api.ChatColor;

public class Sidebar implements Closeable {
	private Main main = null;
	private Scoreboard scoreboard = null;
	private Objective objective = null;
	private String objective_name = "TagGame";
	private String dispray_name = "TagGame";
	private Map<SidebarContents, Integer> lines = new EnumMap<>(SidebarContents.class);

	// public Sidebar(Main main, int game_length, int runner_count, int hunter_count) {
	// initialize(main, game_length, runner_count, hunter_count, objective_name, dispray_name);
	// }

	public Sidebar(Main main, int runner_count, int hunter_count, String objective_name, String dispray_name) {
		initialize(main, runner_count, hunter_count,
				this.objective_name + "_" + objective_name, this.dispray_name + "_" + dispray_name);
	}

	private void initialize(Main main, int runner_count, int hunter_count, String objective_name, String dispray_name) {
		// TODO: Game複数実行時は第一引数を変更。
		this.main = main;
		scoreboard = main.getScoreBoard();
		this.objective_name = objective_name;
		this.dispray_name = dispray_name;
		objective = scoreboard.registerNewObjective(objective_name, "dummy");
		objective.setDisplayName(dispray_name);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		update(runner_count, hunter_count);
		Bukkit.getOnlinePlayers().forEach(player -> player.setScoreboard(scoreboard));
		SendLog.debug("Setted scoreboard!");
	}

	public enum SidebarContents {
		Hunter(0), Runner(-1);

		public int line;

		private SidebarContents(int line) {
			this.line = line;
		}
	}

	public void update(int runner_count, int hunter_count) {
		lines.put(SidebarContents.Runner, runner_count);
		lines.put(SidebarContents.Hunter, hunter_count);

		removeLines();

		// set new lines.
		for (SidebarContents content : SidebarContents.values()) {
			objective.getScore(getText(content) + ChatColor.RESET + ": " + lines.get(content)).setScore(content.line);
		}
	}

	public void removeLines() {
		objective.getScoreboard().getEntries().forEach(entry -> objective.getScoreboard().resetScores(entry));
	}

	public String getText(SidebarContents contents) {
		switch (contents) {
			case Hunter:
				return main.configfile.Hunter.name;
			case Runner:
				return main.configfile.Runner.name;
			default:
				return main.configfile.Hunter.name;
		}
	}

	@Override
	public void close() {
		scoreboard.clearSlot(DisplaySlot.SIDEBAR);
		objective.unregister();
	}
}
