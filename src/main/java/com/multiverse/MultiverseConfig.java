package com.multiverse;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Multiverse")
public interface MultiverseConfig extends Config
{
	@ConfigItem(
		keyName = "realm",
		name = "Default Realm",
		description = "The realm you wish to be in"
	)
	default String realm()
	{
		return "General";
	}
}
