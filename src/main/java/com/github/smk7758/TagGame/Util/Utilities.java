package com.github.smk7758.TagGame.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Utilities {
	private Utilities() {
	}

	public static String convertText(String text, CommandSender sender) {
		text = text.replaceAll("%Player%", sender.getName());
		if (sender instanceof Player) {
			Player player = (Player) sender;
			text = text.replaceAll("%World%", player.getWorld().getName());
		}
		text = ChatColor.translateAlternateColorCodes('&', text);
		return text;
	}

	public static String convertText(String text, String... replaces) {
		String rep = "";
		for (String replace : replaces) {
			if (rep == null || rep.isEmpty()) {
				rep = replace;
			} else {
				text = text.replaceAll(rep, replace);
				SendLog.debug(text);
				rep = "";
			}
		}
		return text;
	}

	/**
	 * @param text 置き換えが実行される文字列。
	 * @param sender 送信者
	 * @param replaces 交互に置き換えが実行される文字列と置き換えする文字列を指定。
	 * @return 置き換えられた文字列。
	 */
	public static String convertText(String text, CommandSender sender, String... replaces) {
		return convertText(convertText(text, sender), replaces);
	}
}
