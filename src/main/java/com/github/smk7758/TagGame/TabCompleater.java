package com.github.smk7758.TagGame;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class TabCompleater implements TabCompleter {
	public Main main = null;
	private static final List<String> list_player = new ArrayList<>(), list_top = new ArrayList<>();
	static {
		{
			list_player.add("prison");
			list_player.add("hunter");
			list_player.add("hunter");
			list_player.add("runner");
			list_player.add("otherplayer");
		}
		{
			list_top.add("set");
			list_top.add("remove");
			list_top.add("show");
			list_top.add("start");
			list_top.add("stop");
			list_top.add("out");
			list_top.add("caught");
			list_top.add("addpage");
			list_top.add("save");
			list_top.add("reload");
			list_top.add("help");
			list_top.add("debug");
		}
	}

	public TabCompleater(Main main) {
		this.main = main;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("TosoGame")) {
			List<String> list = new ArrayList<>();
			if (args.length <= 0) {
				return list_top;
			}
			if (args[0].equalsIgnoreCase("set")) {
				if (args.length <= 1) {
					return list_player;
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (args.length <= 2) {
					return list_player;
				}
			} else if (args[0].equalsIgnoreCase("show")) {
				if (args.length <= 1) {
					return list_player;
				}
			} else if (args[0].equalsIgnoreCase("start")) {
				return list;
			} else if (args[0].equalsIgnoreCase("stop")) {
				return list;
			} else if (args[0].equalsIgnoreCase("out")) {
				return list;
			} else if (args[0].equalsIgnoreCase("caught")) {
				return list;
			} else if (args[0].equalsIgnoreCase("addpage")) {
				return list;
			} else if (args[0].equalsIgnoreCase("save")) {
				return list;
			} else if (args[0].equalsIgnoreCase("reload")) {
				return list;
			} else if (args[0].equalsIgnoreCase("help")) {
				return list;
			} else if (args[0].equalsIgnoreCase("debug")) {
				list.add("now");
				return list;
			} else {
				return list_top;
			}
		}
		return null;
	}

}
