package net.skeagle.vrncore.GUIs;

import net.skeagle.vrncore.utils.VRNUtil;
import net.skeagle.vrncore.warps.Warp;
import net.skeagle.vrncore.warps.WarpManager;
import net.skeagle.vrnlib.inventorygui.InventoryGUI;
import net.skeagle.vrnlib.inventorygui.ItemButton;
import net.skeagle.vrnlib.inventorygui.PaginationPanel;
import net.skeagle.vrnlib.itemutils.ItemBuilder;
import net.skeagle.vrnlib.messages.Messages;
import net.skeagle.vrnlib.misc.Task;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public class WarpsGUI {

    public WarpsGUI(WarpManager manager, Player player) {
        BorderedGUI gui = new BorderedGUI("Global warps list");
        PaginationPanel panel = gui.paginate();
        CompletableFuture.runAsync(() ->
                manager.getWarps().forEach(w ->
                        panel.addPagedButton(ItemButton.create(getIcon(w), e -> {
                            if (e.getClick().isLeftClick()) {
                                Task.syncDelayed(() -> player.closeInventory());
                                VRNUtil.say(player, Messages.msg("teleporting"));
                                player.teleport(w.getLocation());
                            }
                            if (e.getClick().isRightClick() && !w.getOwner().equals(player.getUniqueId())) {
                                if (!player.hasPermission("vrn.delwarp.others")) {
                                    player.closeInventory();
                                    VRNUtil.say(player, VRNUtil.NOPERM);
                                    return;
                                }
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5f, 0.5f);
                                deleteConfirm(manager, player, w);
                            }
                        }))
                )).thenRun(() -> Task.syncDelayed(() -> gui.open(player)));
    }

    private ItemStack getIcon(Warp w) {
        Block b = VRNUtil.getStandingBlock(w.getLocation());
        return new ItemBuilder(b != null ? b.getType() : Material.BARRIER).setName("&7" + w.getName());
    }

    private void deleteConfirm(WarpManager manager, Player player, Warp w) {
        InventoryGUI gui = new InventoryGUI(9, "&c&lConfirm delete?");
        gui.addButton(ItemButton.create(new ItemBuilder(Material.RED_WOOL).setName("&cCancel (Back to warps list)"), e -> new WarpsGUI(manager, player)), 2);
        gui.getInventory().setItem(4, new ItemBuilder(Material.MAP).setName("&6Are you sure?").setLore("", "&eAre you sure you want to", "&edelete this warp?"));
        gui.addButton(ItemButton.create(new ItemBuilder(Material.LIME_WOOL).setName("&aConfirm"), e -> {
            manager.deleteWarp(w);
            Task.syncDelayed(() -> player.closeInventory());
            VRNUtil.say(player, "&7Warp &a" + w.getName() + "&7 successfully deleted.");
        }), 6);
        gui.open(player);
    }
}
