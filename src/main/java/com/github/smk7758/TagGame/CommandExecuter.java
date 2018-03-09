package com.github.smk7758.TagGame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.smk7758.TagGame.Files.YamlFileManager;
import com.github.smk7758.TagGame.Game.ScorebordTeam.TeamName;
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
			if (args.length <= 0) {
				SendLog.error(main.languagefile.lessCommandArguments, sender);
				showCommandList(sender);
				return false;
			}
			if (args[0].equalsIgnoreCase("set")) {
				if (args.length <= 1) {
					return false;
				}
				if (args[1].equalsIgnoreCase("prison")) {
					if (sender instanceof Player) {
						Location loc = ((Player) sender).getLocation();
						main.getGameManager().gamefile.prison_loc = loc;
						SendLog.send("Prison has been set to: "
								+ "x: " + loc.getX() + " y: " + loc.getY() + " z: " + loc.getZ(), sender);
					} else {
						SendLog.error(main.languagefile.mustSendFromPlayer, sender);
						return false;
					}
				}
				if (args[1].equalsIgnoreCase("hunter")) {
					setTeam(TeamName.Hunter, args[2], sender);
				} else if (args[1].equalsIgnoreCase("runner")) {
					setTeam(TeamName.Runner, args[2], sender);
				} else if (args[1].equalsIgnoreCase("otherplayer")) {
					setTeam(TeamName.OtherPlayer, args[2], sender);
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (args.length <= 2) {
					return false;
				}
				if (args[1].equalsIgnoreCase("hunter")) {
					removeTeam(TeamName.Hunter, args[2], sender);
				} else if (args[1].equalsIgnoreCase("runner")) {
					removeTeam(TeamName.Runner, args[2], sender);
				} else if (args[1].equalsIgnoreCase("otherplayer")) {
					removeTeam(TeamName.OtherPlayer, args[2], sender);
				}
			} else if (args[0].equalsIgnoreCase("show")) {
				if (args.length <= 1) {
					for (TeamName name : TeamName.values()) {
						showTeam(name, sender);
					}
					return false;
				}
				if (args[1].equalsIgnoreCase("hunter")) {
					showTeam(TeamName.Hunter, sender);
				} else if (args[1].equalsIgnoreCase("runner")) {
					showTeam(TeamName.Runner, sender);
				} else if (args[1].equalsIgnoreCase("otherplayer")) {
					showTeam(TeamName.OtherPlayer, sender);
				} else if (args[1].equalsIgnoreCase("prison")) {
					SendLog.send("World:" + main.gamefile.prison_loc.getWorld().getName());
					SendLog.send("X:" + main.gamefile.prison_loc.getX());
					SendLog.send("Y:" + main.gamefile.prison_loc.getY());
					SendLog.send("Z:" + main.gamefile.prison_loc.getZ());
				}
			} else if (args[0].equalsIgnoreCase("start")) {
				if (main.getGameManager().start()) SendLog.send(main.languagefile.startCommand, sender);
				else SendLog.error(main.languagefile.startCommandError, sender);
			} else if (args[0].equalsIgnoreCase("stop")) {
				if (main.getGameManager().stop()) SendLog.send(main.languagefile.stopCommand, sender);
				else SendLog.error(main.languagefile.stopCommandError, sender);
			} else if (args[0].equalsIgnoreCase("out")) {
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
				main.getGameManager().out(player_out);
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
			} else if (args[0].equalsIgnoreCase("addpage")) {
				if (main.getGameManager().addNextPage()) SendLog.send(main.languagefile.addBook, sender);
				else SendLog.error(main.languagefile.addBookError, sender);
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
			} else if (args[0].equalsIgnoreCase("test0")) {
				main.configfile.testPlayers();
				main.configfile.loadPlayers();
				main.configfile.testPlayers();
			} else if (args[0].equalsIgnoreCase("test1")) {
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
		boolean success = main.getGameManager().getTeam().setTeam(name, player_name);
		if (success) SendLog.send(Utilities.convertText(main.languagefile.setTeam, "%Player%",
				player_name, "%Team%", name.toString()), sender);
		else SendLog.error(Utilities.convertText(main.languagefile.setTeamError, "%Player%",
				player_name, "%Team%", name.toString()), sender);
	}

	public void removeTeam(TeamName name, String player_name, CommandSender sender) {
		boolean success = main.getGameManager().getTeam().removeTeam(name, player_name);
		if (success) SendLog.send(Utilities.convertText(main.languagefile.removeTeam, "%Player%",
				player_name, "%Team%", name.toString()), sender);
		else SendLog.error(Utilities.convertText(main.languagefile.removeTeamError, "%Player%",
				player_name, "%Team%", name.toString()), sender);
	}

	public void showTeam(TeamName name, CommandSender sender) {
		SendLog.send("Team: " + name, sender);
		main.getGameManager().getTeam().getTeamPlayers(name)
				.forEach(player -> SendLog.send(player.getName(), sender));
	}

	private void showCommandList(CommandSender sender) {
		SendLog.send("<TosoGame Command List>", sender);
		SendLog.send("TosoGame (help)", sender);
		SendLog.send("-- show you command list.", sender);
		SendLog.send("TosoGame set prison", sender);
		SendLog.send("-- set prison.", sender);
		SendLog.send("TosoGame set <hunter, runner, otherplayer> <player name>", sender);
		SendLog.send("-- set player to each team.", sender);
		SendLog.send("TosoGame remove <hunter, runner, otherplayer> <player name>", sender);
		SendLog.send("-- remove player from each team.", sender);
		SendLog.send("TosoGame show <hunter, runner, otherplayer> (<player name>)", sender);
		SendLog.send("-- show which player is in the team.", sender);
		SendLog.send("TosoGame start", sender);
		SendLog.send("-- start TosoGame!", sender);
		SendLog.send("TosoGame stop", sender);
		SendLog.send("-- stop TosoGame", sender);
	}
}