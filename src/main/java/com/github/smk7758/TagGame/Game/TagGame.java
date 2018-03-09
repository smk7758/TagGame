package com.github.smk7758.TagGame.Game;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.github.smk7758.TagGame.Main;
import com.github.smk7758.TagGame.Files.DataFiles.GameFile;
import com.github.smk7758.TagGame.Game.ScorebordTeam.TeamName;
import com.github.smk7758.TagGame.Util.SendLog;
import com.github.smk7758.TagGame.Util.Utilities;

public class TagGame implements Listener {
	private Main main = null;
	private boolean is_game_starting = false;

	private int time_count = 1;
	private int wait_time = 10;
	public List<Location> hunter_loc = null;
	private BukkitTask loop = null, finish = null;

	private ScorebordTeam team = null;
	private Sidebar sidebar = null;
	public GameFile gamefile = null;

	public TagGame(Main main) {
		this.main = main;
		this.gamefile = main.gamefile; // TODO
		team = new ScorebordTeam(main);
	}

	public boolean start() {
		if (!canStart()) return false;

		time_count = gamefile.GameLength.getAsSecond();
		wait_time = gamefile.TeleportWaitTime;
		sidebar = new Sidebar(main, time_count, team.getAllRunner().size(),
				team.getTeamPlayers(TeamName.Hunter).size(), main.gamefile.GameName, main.gamefile.GameName);

		List<? extends Player> online_players = (List<? extends Player>) Bukkit.getOnlinePlayers();
		online_players.removeIf(player -> !player.getGameMode().equals(GameMode.ADVENTURE));
		if (team.getTeamPlayers(TeamName.Hunter).size() < 1) {
			Collections.shuffle(online_players);
			team.setTeam(TeamName.Hunter, online_players.get(0));
		}
		if (team.getTeamPlayers(TeamName.Runner).size() < 1) {
			online_players.forEach(player -> team.setTeam(TeamName.Runner, player));
		}

		switchIsGameStarting();
		loop();
		finish();

		clearInventoryies(TeamName.Runner);
		clearInventoryies(TeamName.Hunter);
		removeAllPotionEffects(TeamName.Hunter);
		team.getTeamPlayers(TeamName.Hunter).forEach(player -> player.setTotalExperience(8));// TEST
		team.getTeamPlayers(TeamName.Runner).forEach(player -> player.setTotalExperience(8));// TEST

		team.getTeamPlayers(TeamName.Hunter).forEach(player -> player.getInventory().addItem(getHunterItems()));

		// send start!
		team.sendTeamPlayers(TeamName.Hunter, main.languagefile.startToHunter);
		team.sendTeamPlayers(TeamName.Runner, main.languagefile.startToRunner);
		return true;
	}

	public void loop() {
		loop = new BukkitRunnable() {
			PotionEffect potion_effect = new PotionEffect(PotionEffectType.SPEED, Short.MAX_VALUE, 0);

			@Override
			public void run() {
				if (0 == team.getTeamPlayers(TeamName.Runner).size()) {
					finishByCaught();
				} else {
					time_count -= 1;
					sidebar.update(time_count,
							team.getTeamPlayers(TeamName.Runner).size(),
							team.getTeamPlayers(TeamName.RunnerPrisoner).size(),
							team.getTeamPlayers(TeamName.Hunter).size());
					team.getTeamPlayers(TeamName.Hunter)
							.forEach(player -> player.addPotionEffect(potion_effect));
					team.getTeamPlayers(TeamName.Hunter)
							.forEach(player -> player
									.setTotalExperience(time_count / gamefile.GameLength.getAsSecond() * 8));
					team.getTeamPlayers(TeamName.Runner)
							.forEach(player -> player
									.setTotalExperience(time_count / gamefile.GameLength.getAsSecond() * 8));

				}
			}
		}.runTaskTimer(main, 0, 1 * 20);
	}

	public void finish() {
		finish = new BukkitRunnable() {
			@Override
			public void run() {
				if (isGameStarting()) {
					team.sendTeamPlayers(TeamName.Hunter, main.languagefile.finishByTimeToHunter);
					team.sendTeamPlayers(TeamName.Runner, main.languagefile.finishByTimeToRunner);
					team.sendTeamPlayers(TeamName.RunnerPrisoner, main.languagefile.finishByTimeToRunner);
					close();
				}
			}
		}.runTaskLater(main, gamefile.GameLength.getAsSecond() * 20);
	}

	public boolean finishByCaught() {
		// TODO: 直で出すのはなんか物足りない。
		if (!isGameStarting()) return false;
		team.sendTeamPlayers(TeamName.Hunter, main.languagefile.finishByCaughtToHunter);
		team.sendTeamPlayers(TeamName.Runner, main.languagefile.finishByCaughtToRunner);
		team.sendTeamPlayers(TeamName.RunnerPrisoner, main.languagefile.finishByCaughtToRunner);
		close();
		return true;
	}

	public boolean stop() {
		if (!isGameStarting()) return false;
		team.sendTeamPlayers(TeamName.Hunter, main.languagefile.stop);
		team.sendTeamPlayers(TeamName.Runner, main.languagefile.stop);
		close();
		return true;
	}

	private boolean close() {
		if (!isGameStarting()) return false;
		clearInventoryies(TeamName.Runner);
		clearInventoryies(TeamName.RunnerPrisoner);
		removeAllPotionEffects(TeamName.Hunter);

		team.clearTeam();
		loop.cancel();
		finish.cancel();
		sidebar.close();
		switchIsGameStarting();
		return true;
	}

	@EventHandler
	public void onPlayerAttackPlayer(EntityDamageByEntityEvent event) {
		if (!isGameStarting()) return;
		Entity attacker_ = event.getDamager();
		Entity damager_ = event.getEntity();
		if (!(attacker_ instanceof Player && damager_ instanceof Player)) return;
		Player attacker = (Player) attacker_;
		Player damager = (Player) damager_;
		if (!(getTeam().isTeam(TeamName.Hunter, attacker)
				&& getTeam().isTeam(TeamName.Runner, damager))) return;
		// --- finish check ---

		caught(damager);
	}

	@EventHandler
	public void onPlayerUseItem(PlayerInteractEvent event) {
		if (event.getItem() == null) return;
		if (!event.getItem().hasItemMeta()) return;
		if (event.getItem().getItemMeta().getDisplayName() == null) return;
		Player player = event.getPlayer();
		if (!team.isTeam(TeamName.Runner, player)) return;
		if (event.getItem().getItemMeta().getDisplayName().equals(main.gamefile.HunterItems.Feather.Name)
				&& event.getItem().getItemMeta().getLore().equals(main.gamefile.HunterItems.Feather.Lore)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5, 0));
		} else if (event.getItem().getItemMeta().getDisplayName().equals(main.gamefile.HunterItems.Bone.Name)
				&& event.getItem().getItemMeta().getLore().equals(main.gamefile.HunterItems.Bone.Lore)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5, 0));
			// TODO effect or bukkit invisible
		}
	}

	public void caught(Player player) {
		if (!isGameStarting()) return;
		SendLog.debug("Player: " + player.getName() + " has been caught.");
		// send to player
		SendLog.send(Utilities.convertText(main.languagefile.catchRunnerToPlayer, player), player);
		// send mail
		getTeam().sendTeamPlayers(TeamName.Hunter,
				Utilities.convertText(main.languagefile.catchRunnerToOthers, player));
		getTeam().sendTeamPlayers(TeamName.Runner,
				Utilities.convertText(main.languagefile.catchRunnerToOthers, player));
		getTeam().sendTeamPlayers(TeamName.RunnerPrisoner,
				Utilities.convertText(main.languagefile.catchRunnerToOthers, player));

		// change team
		getTeam().changeTeam(player, TeamName.RunnerPrisoner);

		// teleport
		SendLog.send(
				main.languagefile.convertText(main.languagefile.catchRunnerWaitTeleportToPlayer, player, wait_time),
				player);
		teleportDelay(player);
	}

	public void out(Player player) {
		if (!isGameStarting()) return;
		getTeam().changeTeam(player, TeamName.OtherPlayer);
		SendLog.debug("Player: " + player.getName() + " has been out.");
		SendLog.send(Utilities.convertText(main.languagefile.outRunnerToPlayer, player), player);

		getTeam().sendTeamPlayers(TeamName.Runner,
				Utilities.convertText(main.languagefile.outRunnerToOthers, player));
		getTeam().sendTeamPlayers(TeamName.Hunter,
				Utilities.convertText(main.languagefile.outRunnerToOthers, player));
	}

	private void teleportDelay(Player damager) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (isGameStarting()) {
					SendLog.debug("Player: " + damager.getName() + " has been teleported to the prison.");
					damager.teleport(main.getGameManager().gamefile.prison_loc);
				} else {
					SendLog.debug("The game has been already finished, so woun't tp.");
				}
			}
		}.runTaskLater(main, wait_time * 20);
	}

	private void clearInventoryies(TeamName name) {
		team.getTeamPlayers(name).forEach(player -> player.getInventory().clear());
	}

	private boolean canStart() {
		boolean can_start = true;
		if (isGameStarting()) can_start = false;
		if (gamefile.prison_loc == null) {
			SendLog.error(main.languagefile.startCheckNotSetPrison);
			can_start = false;
		}
		if (0 == team.getTeamPlayers(TeamName.Runner).size()
				|| 0 == team.getTeamPlayers(TeamName.Hunter).size()) {
			SendLog.error(main.languagefile.startCheckNoPlayers);
			can_start = false;
		}
		return can_start;
	}

	private void removeAllPotionEffects(TeamName name) {
		// remove name's all potion effect
		getTeam().getTeamPlayers(name).forEach(player -> player.getActivePotionEffects()
				.forEach(potion_effect -> player.removePotionEffect(potion_effect.getType())));
	}

	private ItemStack[] getHunterItems() {
		ItemStack[] items = {};
		items[0] = getFeather();
		items[1] = getBone();
		return items;
	}

	private ItemStack getFeather() {
		ItemStack itemstack = new ItemStack(Material.FEATHER);
		ItemMeta itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(main.gamefile.HunterItems.Feather.Name);
		itemmeta.setLore(main.gamefile.HunterItems.Feather.Lore);
		itemstack.setItemMeta(itemmeta);
		return itemstack;
	}

	private ItemStack getBone() {
		ItemStack itemstack = new ItemStack(Material.BONE);
		ItemMeta itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(main.gamefile.HunterItems.Bone.Name);
		itemmeta.setLore(main.gamefile.HunterItems.Bone.Lore);
		itemstack.setItemMeta(itemmeta);
		return itemstack;
	}

	public boolean isGameStarting() {
		return is_game_starting;
	}

	private void switchIsGameStarting() {
		is_game_starting = !is_game_starting;
	}

	public ScorebordTeam getTeam() {
		return team;
	}

	public Sidebar getSidebar() {
		return sidebar;
	}
}