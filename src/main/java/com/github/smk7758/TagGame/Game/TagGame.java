package com.github.smk7758.TagGame.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.github.smk7758.TagGame.Main;
import com.github.smk7758.TagGame.Files.DataFiles.GameFile;
import com.github.smk7758.TagGame.Game.TeamManager.TeamName;
import com.github.smk7758.TagGame.Util.SendLog;
import com.github.smk7758.TagGame.Util.Utilities;

public class TagGame implements Listener {
	private Main main = null;
	private boolean is_game_starting = false;

	private int time_count = 1;
	private int wait_time = 10;
	public List<Location> hunter_loc = null;
	private BukkitTask loop = null, finish = null;

	private TeamManager team = null;
	private Sidebar sidebar = null;
	public GameFile gamefile = null;

	public TagGame(Main main) {
		this.main = main;
		this.gamefile = main.gamefile; // TODO
		team = new TeamManager(main);
	}

	public boolean start() {
		if (!canStart()) return false;

		time_count = gamefile.GameLength.getAsSecond();
		wait_time = gamefile.TeleportWaitTime;

		SendLog.debug("TagGame start state.");
		SendLog.debug("OnlinePlayers size: " + Bukkit.getOnlinePlayers().size());

		final boolean player_management_success = managePlayers();
		if (!player_management_success) return false;

		SendLog.debug("<Show Teams>");
		for (TeamName name : TeamName.values()) {
			SendLog.debug("Team: " + name.toString());
			main.getGameManager().getTeamManager().getTeamPlayers(name)
					.forEach(player -> SendLog.debug(" - " + player.getName()));
		}

		// clear and remove the inv and effects
		clearInventoryies(TeamName.Runner);
		clearInventoryies(TeamName.Hunter);
		removeAllPotionEffects(TeamName.Hunter);

		// set exp
		team.getAllTeamPlayers().forEach(player -> player.setLevel(time_count));

		// addHunterItems
		// TODO
		team.getTeamPlayers(TeamName.Runner).forEach(player -> player.getInventory().addItem(getRunnerItems()));

		// TODO: HideName

		// Sidebar
		sidebar = new Sidebar(main, team.getTeamPlayers(TeamName.Runner).size(),
				team.getTeamPlayers(TeamName.Hunter).size(), gamefile.GameName, gamefile.GameName);

		// TP Runner
		teleportPlayers(TeamName.Runner, gamefile.spawn_loc);
		// TP Hunter
		Bukkit.getScheduler().runTaskLater(main, () -> {
			teleportPlayers(TeamName.Hunter, gamefile.spawn_loc);
		}, wait_time * 20);

		// send start!
		team.sendTeamPlayers(TeamName.Hunter, main.languagefile.startToHunter);
		team.sendTeamPlayers(TeamName.Runner, main.languagefile.startToRunner);

		loop();
		callFinish();
		switchIsGameStarting();

		SendLog.debug("Started the game in " + gamefile.GameLength.getAsSecond() + " second.");

		return true;
	}

	private boolean canStart() {
		boolean can_start = true;
		if (isGameStarting()) can_start = false;
		if (gamefile.lobby_loc == null || gamefile.spawn_loc == null || gamefile.respawn_loc == null) {
			SendLog.error(main.languagefile.startCheckNotSetted);
			can_start = false;
		}
		return can_start;
	}

	private void teleportPlayers(TeamName name, Location loc) {
		team.getTeamPlayers(name).forEach(player -> player.teleport(loc));
	}

	private boolean managePlayers() {
		List<Player> online_players = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		online_players.removeIf(player -> !player.getGameMode().equals(GameMode.ADVENTURE));

		if (online_players.size() < 2) {
			SendLog.error(main.languagefile.lessPlayersOfAdventure);
			return false;
		}
		if (team.getTeamPlayers(TeamName.Hunter).size() < 1) {
			Collections.shuffle(online_players);
			// set one player to Hunter
			team.setTeam(TeamName.Hunter, online_players.get(0));
			// send you are Hunter
			SendLog.send(main.languagefile.setToHunterOnStart, online_players.get(0));
		}
		if (team.getTeamPlayers(TeamName.Runner).size() < 1) {
			// remove Hunter
			online_players.removeIf(player -> team.isTeam(TeamName.Hunter, player));
			// set Runner
			online_players.forEach(player -> team.setTeam(TeamName.Runner, player));
			// send you are Runner
			online_players.forEach(player -> SendLog.send(main.languagefile.setToRunnerOnStart, player));
		}
		return true;
	}

	public void loop() {
		loop = new BukkitRunnable() {
			private final PotionEffect potion_effect = new PotionEffect(PotionEffectType.SPEED, Short.MAX_VALUE, 0);

			@Override
			public void run() {
				if (isGameStarting()) {
					time_count -= 1;
					sidebar.update(
							team.getTeamPlayers(TeamName.Runner).size(),
							team.getTeamPlayers(TeamName.Hunter).size());

					if (time_count < 0) {
						finishByTimeLimit();
						return;
					}

					// final float exp_amount = (float) ((double) time_count / (double) gamefile.GameLength.getAsSecond());
					team.getTeamPlayers(TeamName.Hunter).forEach(player -> player.setLevel(time_count));
					team.getTeamPlayers(TeamName.Runner).forEach(player -> player.setLevel(time_count));

					team.getTeamPlayers(TeamName.Hunter).forEach(player -> player.addPotionEffect(potion_effect));

					SendLog.debug("LeftTime: " + time_count);
					// SendLog.debug("Percent: " + exp_amount);
				}
			}
		}.runTaskTimer(main, 0, 1 * 20);
	}

	public boolean finishByCaught() {
		// TODO: 直で出すのはなんか物足りない。
		if (!isGameStarting()) return false;
		team.sendAllTeamPlayers(main.languagefile.finishByCaughtToRunner);
		close();
		return true;
	}

	public void callFinish() {
		finish = new BukkitRunnable() {
			@Override
			public void run() {
				finishByTimeLimit();
			}
		}.runTaskLater(main, gamefile.GameLength.getAsSecond() * 20);
	}

	private void finishByTimeLimit() {
		if (isGameStarting()) {
			team.sendAllTeamPlayers(main.languagefile.finishByTimeToHunter);
			close();
		}
	}

	public boolean stop() {
		if (!isGameStarting()) return false;
		team.sendAllTeamPlayers(main.languagefile.stop);
		close();
		return true;
	}

	private boolean close() {
		if (!isGameStarting()) return false;

		sidebar.close(); // better to do first.

		// TP
		team.getAllTeamPlayers().forEach(player -> player.teleport(gamefile.lobby_loc));

		// clears
		clearInventoryies(TeamName.Runner);
		removeAllPotionEffects(TeamName.Hunter);

		// set exp 0
		team.getAllTeamPlayers().forEach(player -> player.setLevel(0));

		// clear Team
		team.clearTeam();
		if (loop != null) {
			loop.cancel();
			loop = null;
		}
		if (finish != null) {
			finish.cancel();
			finish = null;
		}
		switchIsGameStarting();
		return true;
	}

	public void caught(Player player) {
		if (!isGameStarting()) return;
		SendLog.debug("Player: " + player.getName() + " has been caught.");
		// send to player
		SendLog.send(Utilities.convertText(main.languagefile.catchRunnerToPlayer, player), player);
		// send mail
		getTeamManager().sendTeamPlayers(TeamName.Hunter,
				Utilities.convertText(main.languagefile.catchRunnerToOthers, player));
		getTeamManager().sendTeamPlayers(TeamName.Runner,
				Utilities.convertText(main.languagefile.catchRunnerToOthers, player));

		// change team
		changeRunnerToCaughtRunner(player);

		if (team.getTeamPlayers(TeamName.Runner).size() < 1) {
			// 全員捕まった！
			finishByCaught();
			return;
		}

		// teleporting message
		SendLog.send(
				main.languagefile.convertText(main.languagefile.catchRunnerWaitTeleportToPlayer, player, wait_time),
				player);

		// teleport delay
		Bukkit.getScheduler().runTaskLater(main, () -> {
			if (isGameStarting()) {
				SendLog.debug("Player: " + player.getName() + " has been teleported to the respawn.");
				player.teleport(gamefile.respawn_loc);
			} else {
				SendLog.debug("The game has been already finished, so woun't tp.");
			}
		}, wait_time * 20);
	}

	private void changeRunnerToCaughtRunner(Player player_runner) {
		player_runner.getInventory().clear();
		getTeamManager().changeTeam(player_runner, TeamName.CaughtRunner);
	}

	private void clearInventoryies(TeamName name) {
		team.getTeamPlayers(name).forEach(player -> player.getInventory().clear());
	}

	private void removeAllPotionEffects(TeamName name) {
		// remove name's all potion effect
		// TeamNamePlayers -> get active effects -> remove (= Remove All Effects)
		team.getTeamPlayers(name).forEach(player -> {
			if (player != null) player.getActivePotionEffects()
					.forEach(potion_effect -> player.removePotionEffect(potion_effect.getType()));
		});
	}

	public ItemStack[] getRunnerItems() {
		ItemStack[] items = new ItemStack[2];
		items[0] = getFeather();
		items[1] = getBone();
		return items;
	}

	public ItemStack getFeather() {
		ItemStack itemstack = new ItemStack(Material.FEATHER);
		ItemMeta itemmeta = itemstack.getItemMeta();
		SendLog.debug("Feather name: " + gamefile.RunnerItems.Feather.Name);
		itemmeta.setDisplayName(gamefile.RunnerItems.Feather.Name);
		itemmeta.setLore(gamefile.RunnerItems.Feather.Lore);
		itemstack.setItemMeta(itemmeta);
		return itemstack;
	}

	public ItemStack getBone() {
		ItemStack itemstack = new ItemStack(Material.BONE);
		ItemMeta itemmeta = itemstack.getItemMeta();
		SendLog.debug("Bone name: " + gamefile.RunnerItems.Bone.Name);
		itemmeta.setDisplayName(gamefile.RunnerItems.Bone.Name);
		itemmeta.setLore(gamefile.RunnerItems.Bone.Lore);
		itemstack.setItemMeta(itemmeta);
		return itemstack;
	}

	public boolean isGameStarting() {
		return is_game_starting;
	}

	private void switchIsGameStarting() {
		is_game_starting = !is_game_starting;
	}

	public TeamManager getTeamManager() {
		return team;
	}

	public Sidebar getSidebar() {
		return sidebar;
	}
}