package com.github.smk7758.TagGame.Game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.smk7758.TagGame.Main;
import com.github.smk7758.TagGame.Game.TeamManager.TeamName;
import com.github.smk7758.TagGame.Util.SendLog;

public class GameListener implements Listener {
	private Main main = null;

	public GameListener(Main main) {
		this.main = main;
	}

	@EventHandler
	public void onPlayerAttackPlayer(EntityDamageByEntityEvent event) {
		if (!main.getGameManager().isGameStarting()) return;
		final Entity attacker_ = event.getDamager();
		final Entity damager_ = event.getEntity();
		if (!(attacker_ instanceof Player && damager_ instanceof Player)) return;
		final Player attacker = (Player) attacker_;
		final Player damager = (Player) damager_;
		if (!(main.getGameManager().getTeamManager().isTeam(TeamName.Hunter, attacker)
				&& main.getGameManager().getTeamManager().isTeam(TeamName.Runner, damager))) return;
		// --- finish check ---

		main.getGameManager().caught(damager);
	}

	@EventHandler
	public void onPlayerUseItem(PlayerInteractEvent event) {
		if (event.getItem() == null) return;
		if (!event.getItem().hasItemMeta()) return;
		if (event.getItem().getItemMeta().getDisplayName() == null) return;
		final Player player = event.getPlayer();
		if (!main.getGameManager().getTeamManager().isTeam(TeamName.Runner, player)) return;
		if (event.getItem().getType().equals(Material.FEATHER)
				&& event.getItem().getItemMeta().getDisplayName().equals(main.gamefile.RunnerItems.Feather.Name)
				&& event.getItem().getItemMeta().getLore().equals(main.gamefile.RunnerItems.Feather.Lore)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 20, 0));
			SendLog.debug("used feather", player);
			player.getInventory().remove(main.getGameManager().getFeather());
			SendLog.debug("removed feather", player);
		} else if (event.getItem().getType().equals(Material.BONE)
				&& event.getItem().getItemMeta().getDisplayName().equals(main.gamefile.RunnerItems.Bone.Name)
				&& event.getItem().getItemMeta().getLore().equals(main.gamefile.RunnerItems.Bone.Lore)) {
			main.getGameManager().getTeamManager().getTeamPlayers(TeamName.Hunter)
					.forEach(hunter_player -> hunter_player.hidePlayer(player));
			Bukkit.getScheduler().runTaskLater(main, () -> {
				main.getGameManager().getTeamManager().getTeamPlayers(TeamName.Hunter)
						.forEach(hunter_player -> hunter_player.showPlayer(player));
			}, 5 * 20);
			SendLog.debug("used bone", player);
			player.getInventory().remove(main.getGameManager().getBone());
			SendLog.debug("removed bone", player);
		}
	}

}
