package org.prism.autowork.blockhelp;

import net.minecraft.world.item.Item;

import java.util.IdentityHashMap;
import java.util.Map;

public interface BlockHelpProvider {
    BlockHelpInfo getHelp();
}