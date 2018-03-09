package com.github.smk7758.TagGame.Game;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.github.smk7758.TagGame.Main;
import com.github.smk7758.TagGame.Util.SendLog;

public class ScorebordTeam {
	// private Main main = null;
	private Scoreboard scoreboard = null;

	public ScorebordTeam(Main main) {
		// this.main = main;
		scoreboard = main.getScoreBoard();
		for (TeamName name : TeamName.values()) {
			// initialize teams
			Team team = scoreboard.registerNewTeam(name.toString());
			team.setDisplayName(name.displayname);
			team.setPrefix(name.prefix);
		}
	}

	// TODO: staticなんでいつか変更.
	public enum TeamName {
		Hunter("Hunter", "&C"), Runner("Runner", "&A"), RunnerPrisoner("RunnerPrisoner",
				"&3"), OtherPlayer("OtherPlayer", "&F");

		public String displayname, prefix;

		private TeamName(String name, String prefix) {
			this.displayname = name;
			this.prefix = prefix;
		}
	}

	public Scoreboard getScoreBoard() {
		return scoreboard;
	}

	public Team getTeam(TeamName name) {
		return scoreboard.getTeam(name.name());
	}

	public Set<Player> getTeamPlayers(TeamName name) {
		Set<Player> players = new HashSet<>();
		getTeam(name).getEntries().forEach(uuid -> players.add(Bukkit.getPlayer(UUID.fromString(uuid))));
		players.removeIf(player -> player == null);
		return players;
	}

	public Set<Player> getAllRunner() {
		Set<Player> players = new HashSet<>();
		players.addAll(getTeamPlayers(TeamName.Runner));
		players.addAll(getTeamPlayers(TeamName.RunnerPrisoner));
		return players;
	}

	public void sendTeamPlayers(TeamName name, String text) {
		getTeamPlayers(name).forEach(player -> SendLog.send(text, player));
	}

	public boolean isTeam(TeamName name, Player player) {
		if (player == null) throw new IllegalArgumentException("Player is null.");
		return getTeam(name).hasEntry(player.getUniqueId().toString());
	}

	public boolean setTeam(TeamName name, String player_name) {
		Player player = Bukkit.getPlayer(player_name);
		if (player == null) return false;
		setTeam(name, player);
		return true;
	}

	public void setTeam(TeamName name, Player player) {
		if (player == null) throw new IllegalArgumentException("Player is null.");
		getTeam(name).addEntry(player.getUniqueId().toString());
		SendLog.debug(player.getName() + " has been added to Team: " + name.toString());
	}

	public boolean removeTeam(TeamName name, String player_name) {
		Player player = Bukkit.getPlayer(player_name);
		if (player == null) return false;
		return removeTeam(name, player);
	}

	public boolean removeTeam(TeamName name, Player player) {
		if (isTeam(name, player)) {
			getTeam(name).removeEntry(player.getUniqueId().toString());
			SendLog.debug(player.getName() + " has been removed to Team: " + name.toString());
			return true;
		} else {
			SendLog.debug(player.getName() + " is not in the Team: " + name.toString());
			return false;
		}
	}

	public void removeTeamAll(TeamName name) {
		getTeam(name).getEntries().removeAll(getTeam(name).getEntries());
		// getTeam(name).getEntries().forEach(uuid -> getTeam(name).removeEntry(uuid));
	}

	public void clearTeam() {
		for (TeamName name : TeamName.values()) {
			removeTeamAll(name);
		}
	}

	public Set<TeamName> getPlayerWhatTeam(Player player) {
		Set<TeamName> contain_teamnames = new HashSet<>();
		for (TeamName name : TeamName.values()) {
			if (getTeamPlayers(name).contains(player)) {
				contain_teamnames.add(name);
			}
		}
		return contain_teamnames;
	}

	public void changeTeam(Player player, TeamName name_to) {
		getPlayerWhatTeam(player).forEach(name_from -> changeTeam(player, name_to, name_from));
	}

	public void changeTeam(Player player, TeamName name_to, TeamName name_from) {
		removeTeam(name_from, player);
		setTeam(name_to, player);
	}
}