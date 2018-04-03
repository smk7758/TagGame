package com.github.smk7758.TagGame.Game;

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
		Entity attacker_ = event.getDamager();
		Entity damager_ = event.getEntity();
		if (!(attacker_ instanceof Player && damager_ instanceof Player)) return;
		Player attacker = (Player) attacker_;
		Player damager = (Player) damager_;
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
		Player player = event.getPlayer();
		if (!main.getGameManager().getTeamManager().isTeam(TeamName.Runner, player)) return;
		if (event.getItem().getItemMeta().getDisplayName().equals(main.gamefile.HunterItems.Feather.Name)
				&& event.getItem().getItemMeta().getLore().equals(main.gamefile.HunterItems.Feather.Lore)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5, 0));
			SendLog.debug("used feather", player);
		} else if (event.getItem().getItemMeta().getDisplayName().equals(main.gamefile.HunterItems.Bone.Name)
				&& event.getItem().getItemMeta().getLore().equals(main.gamefile.HunterItems.Bone.Lore)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5, 0));
			SendLog.debug("used bone", player);
			// TODO effect or bukkit invisible
		}
	}

}
