package com.github.smk7758.TagGame.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.smk7758.TagGame.Main;
import com.github.smk7758.TagGame.Files.YamlFileManager;
import com.github.smk7758.TagGame.Game.TeamManager.TeamName;
import com.github.smk7758.TagGame.Util.SendLog;
import com.github.smk7758.TagGame.Util.Utilities;

public class CommandExecuter implements CommandExecutor {
	public Main main = null;

	public CommandExecuter(Main main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("TagGame")) {
			if (args.length < 1) {
				SendLog.error(main.languagefile.lessCommandArguments, sender);
				showCommandList(sender);
			} else if (args[0].equalsIgnoreCase("set")) {
				if (args.length < 2) {
					SendLog.error(main.languagefile.lessCommandArguments, sender);
				} else if (args[1].equalsIgnoreCase("lobby")) {
					if (!(sender instanceof Player)) {
						SendLog.error(main.languagefile.mustSendCommandFromPlayer);
					} else {
						main.gamefile.lobby_loc = ((Player) sender).getLocation().getBlock().getLocation();
						SendLog.send(Utilities.convertText(main.languagefile.setLocation, "%Location%", "Lobby"),
								sender);
					}
				} else if (args[1].equalsIgnoreCase("spawn")) {
					if (!(sender instanceof Player)) {
						SendLog.error(main.languagefile.mustSendCommandFromPlayer);
					} else {
						main.gamefile.spawn_loc = ((Player) sender).getLocation().getBlock().getLocation();
						SendLog.send(Utilities.convertText(main.languagefile.setLocation, "%Location%", "Spawn"),
								sender);
					}
				} else if (args[1].equalsIgnoreCase("respawn")) {
					if (!(sender instanceof Player)) {
						SendLog.error(main.languagefile.mustSendCommandFromPlayer);
					} else {
						main.gamefile.respawn_loc = ((Player) sender).getLocation().getBlock().getLocation();
						SendLog.send(Utilities.convertText(main.languagefile.setLocation, "%Location%", "Respawn"),
								sender);
					}
				}
			} else if (args[0].equalsIgnoreCase("add")) {
				if (args.length < 2) {
					SendLog.error(main.languagefile.lessCommandArguments, sender);
				} else {
					setTeam(TeamName.Hunter, args[1], sender);
				}
			} else if (args[0].equalsIgnoreCase("addrun")) {
				if (args.length < 2) {
					SendLog.error(main.languagefile.lessCommandArguments, sender);
				} else {
					setTeam(TeamName.Runner, args[1], sender);
				}
			} else if (args[0].equalsIgnoreCase("start")) {
				if (main.getGameManager().start()) SendLog.send(main.languagefile.startCommand, sender);
				else SendLog.error(main.languagefile.startCommandError, sender);
			} else if (args[0].equalsIgnoreCase("stop")) {
				if (main.getGameManager().stop()) SendLog.send(main.languagefile.stopCommand, sender);
				else SendLog.error(main.languagefile.stopCommandError, sender);
			} else if (args[0].equalsIgnoreCase("caught")) {
				Player player_out = null;
				if (args.length <= 1) {
					if (sender instanceof Player) {
						player_out = (Player) sender;
					} else {
						return false;
					}
				} else {
					player_out = Bukkit.getPlayer(args[1]);
				}
				main.getGameManager().caught(player_out);
			} else if (args[0].equalsIgnoreCase("save")) {
				YamlFileManager.saveYamlFile(main.configfile);
				YamlFileManager.saveYamlFile(main.gamefile);
				SendLog.send(main.languagefile.saveCommand, sender);
			} else if (args[0].equalsIgnoreCase("reload")) {
				YamlFileManager.reloadYamlFile(main.configfile);
				YamlFileManager.reloadYamlFile(main.gamefile);
				SendLog.send(main.languagefile.reloadCommand, sender);
			} else if (args[0].equalsIgnoreCase("help")) {
				showCommandList(sender);
			} else if (args[0].equalsIgnoreCase("debug")) {
				if (!(args.length > 2 && args[1].equalsIgnoreCase("now"))) {
					main.configfile.DebugMode = Main.debug_mode = !Main.debug_mode;
				}
				SendLog.send("DebugMode: " + main.configfile.DebugMode, sender);
				SendLog.debug("test", sender);
			} else {
				SendLog.error(main.languagefile.lessCommandArguments, sender);
				showCommandList(sender);
				return false;
			}
			return true;
		}
		return false;
	}

	public void setTeam(TeamName name, String player_name, CommandSender sender) {
		boolean success = main.getGameManager().getTeamManager().setTeam(name, player_name);
		if (success) SendLog.send(Utilities.convertText(main.languagefile.setTeam, "%Player%",
				player_name, "%Team%", name.toString()), sender);
		else SendLog.error(Utilities.convertText(main.languagefile.setTeamError, "%Player%",
				player_name, "%Team%", name.toString()), sender);

		// remove from other team
		for (TeamName name_not : TeamName.values()) {
			if (name != name_not) main.getGameManager().getTeamManager().removeTeam(name, player_name);
		}
	}

	public void removeTeam(TeamName name, String player_name, CommandSender sender) {
		boolean success = main.getGameManager().getTeamManager().removeTeam(name, player_name);
		if (success) SendLog.send(Utilities.convertText(main.languagefile.removeTeam, "%Player%",
				player_name, "%Team%", name.toString()), sender);
		else SendLog.error(Utilities.convertText(main.languagefile.removeTeamError, "%Player%",
				player_name, "%Team%", name.toString()), sender);
	}

	public void showTeam(TeamName name, CommandSender sender) {
		SendLog.send("Team: " + name, sender);
		main.getGameManager().getTeamManager().getTeamPlayers(name)
				.forEach(player -> SendLog.send(player.getName(), sender));
	}

	private void showCommandList(CommandSender sender) {
	}
}