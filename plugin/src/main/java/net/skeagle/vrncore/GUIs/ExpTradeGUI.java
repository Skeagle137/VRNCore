package net.skeagle.vrncore.GUIs;

import net.skeagle.vrnlib.itemutils.ItemUtils;
import net.skeagle.vrnlib.misc.FormatUtils;
import net.skeagle.vrncore.configurable.GuiConfig;
import net.skeagle.vrncore.utils.VRNUtil;
import net.skeagle.vrnlib.inventorygui.InventoryGUI;
import net.skeagle.vrnlib.inventorygui.ItemButton;
import net.skeagle.vrnlib.itemutils.ItemBuilder;
import net.skeagle.vrnlib.misc.Task;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ExpTradeGUI {

    public ExpTradeGUI(final Player player) {
        int size = ((GuiConfig.expTradeMap.size() / 9) + 2) * 9;
        if (size > 54) size = 54;
        final InventoryGUI gui = new InventoryGUI(size, "&9&lExp Trade - Menu");
        List<Material> expTradeMaterials = GuiConfig.expTradeMap.keySet().stream()
                .sorted(Comparator.comparingInt(GuiConfig.expTradeMap::get)).limit(45).toList();
        for (int i = 0; i < GuiConfig.expTradeMap.keySet().size(); i++) {
            ItemBuilder item = new ItemBuilder(expTradeMaterials.get(i));
            final String name = FormatUtils.toTitleCase(item.getType().name().replaceAll("_", " "));
            gui.addButton(ItemButton.create(item.setName("&l" + name)
                    .setLore("&9-----------------------------------------",
                            "&7One &b" + name + " &7is worth &b" + GuiConfig.expTradeMap.get(item.getType()) + " &7exp point(s).",
                            "&9-----------------------------------------"), e -> {
                if (checkInv(player, item.getType())) {
                    new ExpTradeAmount(player, name, item.getType());
                } else {
                    Task.syncDelayed(() -> player.closeInventory());
                    VRNUtil.say(player, "&cThat item is not in your inventory.");
                }
            }), i);
        }
        final int level = player.getLevel();
        final float exp = player.getExp();
        gui.getInventory().setItem(size - 5, new ItemBuilder(Material.MAP).setName("&l&oStats")
                .setLore("&9-----------------------------------------",
                        "&eCurrent Exp level: &6" + level,
                        "&eCurrent next level progress: &6" + String.format("%.0f%%", exp * 100),
                        "&9-----------------------------------------"));
        gui.open(player);
    }

    private boolean checkInv(final Player p, final Material mat) {
        for (final ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == mat)
                return true;
        }
        return false;
    }

    private static class ExpTradeAmount extends InventoryGUI {

        private final int worth;
        private int amount;
        private int displayAmount;
        private int stacks;
        private double total;
        private double gain;

        private ExpTradeAmount(final Player player, final String name, final Material mat) {
            super(9, "&9&lExp Trade - Select Amount");
            this.worth = GuiConfig.expTradeMap.get(mat);
            this.amount = 1;
            this.displayAmount = 1;
            this.total = this.getNewTotal(player);
            this.gain = this.getGain(player);

            this.addButton(ItemButton.create(new ItemBuilder(Material.RED_WOOL).setName("&cCancel (Back to menu)"), e ->
                    new ExpTradeGUI(player)), 2);

            this.addButton(ItemButton.create(new ItemBuilder(mat).setName("&l" + name).setCount(displayAmount)
                    .setLore(getLore()), (e, button) -> {
                amount = e.isShiftClick() ? (e.isLeftClick() ? amount + 10 : amount - 10) : (e.isLeftClick() ? amount + 1 : amount - 1);
                amount = Math.min(this.getAmount(player, mat), amount);
                stacks = amount / 64;
                if (stacks < 1) {
                    amount = Math.max(1, amount);
                }
                displayAmount = amount % 64 != 0 ? amount % 64 : 1;
                total = this.getNewTotal(player);
                gain = this.getGain(player);
                button.setItem(new ItemBuilder(button.getItem()).setCount(displayAmount).setLore(getLore()));
                this.update();
            }), 4);

            this.addButton(ItemButton.create(new ItemBuilder(Material.LIME_WOOL).setName("&aConfirm"), e -> {
                ItemUtils.remove(player.getInventory(), mat, amount);
                player.giveExp(worth * ((amount % 64) + (stacks * 64)));
                VRNUtil.say(player, "You traded &ax" + amount + " " + name + " &7for &a" + FormatUtils.truncateDouble(gain) + " &7exp level(s).");
                Task.syncDelayed(() -> player.closeInventory());
            }), 6);

            this.open(player);
        }

        private int getAmount(final Player p, final Material mat) {
            int total = 0;
            for (final ItemStack item : p.getInventory().getContents()) {
                if (item != null && item.getType() == mat)
                    total += item.getAmount();
            }
            return total;
        }

        private String[] getLore() {
            return new String[] {"&9-----------------------------------------",
            "&fLeft or Right click &7increases/decreases the amount by &b1&7.",
                    "&fShift Left or Right click &7increases/decreases the amount by &b10&7.",
                    "&7Current amount to trade: &d" + (stacks != 0 ? stacks + " &7stack(s)" : "") + (amount % 64 != 0 && stacks != 0 ? " and " : "")
                            + "&b" + (amount % 64 != 0 ? displayAmount : "") + (stacks != 0 ? " &7(&e" + amount + "&7 total)" : ""),
                    "&7Exp gain from trade: &b" + FormatUtils.truncateDouble(gain) + " level(s)",
                    "&7Final Exp level after trade: &a" + FormatUtils.truncateDouble(total),
                    "&9-----------------------------------------"};
        }

        private double getGain(Player player) {
            return this.getNewTotal(player) - player.getLevel() - player.getExp();
        }

        private double getNewTotal(Player player) {
            int level = player.getLevel();
            float progress = player.getExp();
            progress += (float) worth * ((amount % 64) + (stacks * 64)) / this.getExpToNext(level);

            while (progress >= 1) {
                progress = (progress - 1) * this.getExpToNext(level);
                level += 1;
                progress /= this.getExpToNext(level);
            }
            return level + progress;
        }

        private int getExpToNext(final int level) {
            if (level >= 30) {
                return 112 + (level - 30) * 9;
            } else {
                return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
            }
        }
    }
}
