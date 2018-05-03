package com.github.smk7758.TagGame.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.github.smk7758.TagGame.Main;

public class TabCompleater implements TabCompleter {
	public Main main = null;

	public TabCompleater(Main main) {
		this.main = main;

		{
			list_top_args_.add("help");
			list_top_args_.add("add");
			list_top_args_.add("addrun");
			list_top_args_.add("set");
			list_top_args_.add("start");
			list_top_args_.add("stop");
			list_top_args_.add("show");
			list_top_args_.add("save");
			list_top_args_.add("reload");
			list_top_args_.add("debug");
		}
		{
			list_player_types_.add("hunter");
			list_player_types_.add("runner");
			list_player_types_.add("caughtrunner");
		}
		{
			list_locations_.add("lobby");
			list_locations_.add("spawn");
			list_locations_.add("respawn");
		}

	}

	private final List<String> list_top_args_ = new ArrayList<>(),
			list_player_types_ = new ArrayList<>(),
			list_locations_ = new ArrayList<>(),
			list_top_args = Collections.unmodifiableList(list_top_args_),
			list_player_types = Collections.unmodifiableList(list_player_types_),
			list_locations = Collections.unmodifiableList(list_locations_);

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("TagGame")) {
			if (args.length <= 1) {
				return list_top_args;
			} else if (args[0].equalsIgnoreCase("set")) {
				if (args.length <= 2) {
					return list_locations;
				} else {
					return Collections.emptyList();
				}
			} else if (args[0].equalsIgnoreCase("show")) {
				if (args.length <= 2) {
					return list_player_types;
				} else {
					return Collections.emptyList();
				}
			} else if (args[0].equalsIgnoreCase("debug")) {
				if (args.length <= 2) {
					final List<String> list = new ArrayList<>();
					list.add("now");
					return list;
				} else {
					return Collections.emptyList();
				}
			} else if (args[0].equalsIgnoreCase("start")
					|| args[0].equalsIgnoreCase("stop")
					|| args[0].equalsIgnoreCase("save")
					|| args[0].equalsIgnoreCase("reload")
					|| args[0].equalsIgnoreCase("help")) {
				return Collections.emptyList();
			}
		}
		return null;
	}
}
