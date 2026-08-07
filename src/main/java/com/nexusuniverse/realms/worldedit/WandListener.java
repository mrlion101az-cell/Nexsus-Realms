package com.nexusuniverse.realms.worldedit;

import com.nexusuniverse.realms.RealmsConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class WandListener implements Listener {

    private final SelectionManager selections;
    private final RealmsConfig config;

    public WandListener(SelectionManager selections, RealmsConfig config) {
        this.selections = selections;
        this.config = config;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Material wandMaterial = config.worldEditWandMaterial();
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != wandMaterial) return;
        if (event.getClickedBlock() == null) return;
        if (!event.getPlayer().hasPermission("nexusrealms.worldedit.use") && !event.getPlayer().hasPermission("nexusrealms.worldedit.admin")) return;

        Player player = event.getPlayer();
        Selection selection = selections.get(player.getUniqueId());

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            selection.setPos1(event.getClickedBlock().getLocation());
            player.sendMessage(ChatColor.AQUA + "Position 1 set.");
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            selection.setPos2(event.getClickedBlock().getLocation());
            player.sendMessage(ChatColor.AQUA + "Position 2 set.");
        }
    }
}
