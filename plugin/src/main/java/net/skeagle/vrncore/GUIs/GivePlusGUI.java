package net.skeagle.vrncore.GUIs;

import net.skeagle.vrnlib.misc.FormatUtils;
import net.skeagle.vrnlib.inventorygui.InventoryGUI;
import net.skeagle.vrnlib.inventorygui.ItemButton;
import net.skeagle.vrnlib.itemutils.ItemBuilder;
import net.skeagle.vrnlib.misc.Task;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.List;

public class GivePlusGUI {

    private static final List<Material> materialsList = List.of(
            Material.BARRIER, Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK,
            Material.STRUCTURE_VOID, Material.STRUCTURE_BLOCK, Material.JIGSAW, Material.DEBUG_STICK, Material.LIGHT
    );

    public GivePlusGUI(BukkitCommandActor actor, Player player) {
        InventoryGUI gui = new InventoryGUI(9, "&9&l/give+");
        for (int i = 0; i < materialsList.size(); i++) {
            ItemBuilder item = new ItemBuilder(materialsList.get(i));
            gui.addButton(ItemButton.create(item.setName("&7" + FormatUtils.toTitleCase(item.getType().name().replaceAll("_", " "))), e -> {
                Task.syncDelayed(() -> player.closeInventory());
                player.getInventory().addItem(item.setCount(1));
                actor.reply("You have received your item(s).");
            }), i);
        }
        gui.open(player);
    }
}
