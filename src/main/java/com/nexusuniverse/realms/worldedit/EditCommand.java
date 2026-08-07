package com.nexusuniverse.realms.worldedit;

import com.nexusuniverse.realms.RealmsConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditCommand implements CommandExecutor {

    private final SelectionManager selections;
    private final EditPermissionChecker permissions;
    private final EditExecutor executor;
    private final RealmsConfig config;

    public EditCommand(SelectionManager selections, EditPermissionChecker permissions, EditExecutor executor, RealmsConfig config) {
        this.selections = selections;
        this.permissions = permissions;
        this.executor = executor;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "wand" -> handleWand(player);
            case "pos1" -> handlePos1(player);
            case "pos2" -> handlePos2(player);
            case "set" -> handleSet(player, args);
            case "replace" -> handleReplace(player, args);
            case "walls" -> handleWalls(player, args);
            case "outline" -> handleOutline(player, args);
            case "undo" -> handleUndo(player);
            default -> sendUsage(player);
        }
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Usage: /redit <wand|pos1|pos2|set <material>|replace <from> <to>|walls <material>|outline <material>|undo>");
    }

    private void handleWand(Player player) {
        if (!requirePermission(player)) return;
        player.getInventory().addItem(new ItemStack(config.worldEditWandMaterial()));
        player.sendMessage(ChatColor.AQUA + "Left-click to set position 1, right-click for position 2.");
    }

    private void handlePos1(Player player) {
        if (!requirePermission(player)) return;
        selections.get(player.getUniqueId()).setPos1(player.getLocation());
        player.sendMessage(ChatColor.AQUA + "Position 1 set to your location.");
    }

    private void handlePos2(Player player) {
        if (!requirePermission(player)) return;
        selections.get(player.getUniqueId()).setPos2(player.getLocation());
        player.sendMessage(ChatColor.AQUA + "Position 2 set to your location.");
    }

    private void handleSet(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /redit set <material>");
            return;
        }
        Material material = parseMaterial(player, args[1]);
        if (material == null) return;

        Selection selection = validateSelection(player);
        if (selection == null) return;

        int changed = executor.set(player, selection, material);
        player.sendMessage(ChatColor.AQUA + "Set " + changed + " block(s) to " + material.name() + ".");
    }

    private void handleReplace(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /redit replace <from> <to>");
            return;
        }
        Material from = parseMaterial(player, args[1]);
        Material to = parseMaterial(player, args[2]);
        if (from == null || to == null) return;

        Selection selection = validateSelection(player);
        if (selection == null) return;

        int changed = executor.replace(player, selection, from, to);
        player.sendMessage(ChatColor.AQUA + "Replaced " + changed + " block(s) of " + from.name() + " with " + to.name() + ".");
    }

    private void handleWalls(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /redit walls <material>");
            return;
        }
        Material material = parseMaterial(player, args[1]);
        if (material == null) return;

        Selection selection = validateSelection(player);
        if (selection == null) return;

        int changed = executor.walls(player, selection, material);
        player.sendMessage(ChatColor.AQUA + "Built walls out of " + changed + " block(s) of " + material.name() + ".");
    }

    private void handleOutline(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /redit outline <material>");
            return;
        }
        Material material = parseMaterial(player, args[1]);
        if (material == null) return;

        Selection selection = validateSelection(player);
        if (selection == null) return;

        int changed = executor.outline(player, selection, material);
        player.sendMessage(ChatColor.AQUA + "Outlined " + changed + " block(s) with " + material.name() + ".");
    }

    private void handleUndo(Player player) {
        if (!requirePermission(player)) return;
        int restored = executor.undo(player);
        if (restored < 0) {
            player.sendMessage(ChatColor.RED + "Nothing to undo.");
            return;
        }
        player.sendMessage(ChatColor.AQUA + "Restored " + restored + " block(s).");
    }

    /** Shared checks for every actual editing operation: permission, complete selection, in-bounds ownership, and the volume cap. */
    private Selection validateSelection(Player player) {
        if (!requirePermission(player)) return null;

        Selection selection = selections.get(player.getUniqueId());
        if (!selection.isComplete()) {
            player.sendMessage(ChatColor.RED + "Select two positions first -- /redit wand, or /redit pos1 and /redit pos2.");
            return null;
        }

        if (selection.volume() > config.worldEditMaxVolume()) {
            player.sendMessage(ChatColor.RED + "That selection is " + selection.volume() + " blocks -- over the "
                    + config.worldEditMaxVolume() + "-block limit. Select something smaller.");
            return null;
        }

        EditPermissionChecker.Result result = permissions.check(player, selection);
        switch (result) {
            case NO_PERMISSION -> {
                player.sendMessage(ChatColor.RED + "No permission.");
                return null;
            }
            case NOT_YOUR_CLAIM -> {
                player.sendMessage(ChatColor.RED + "That whole selection needs to be inside one of your own personal claims.");
                return null;
            }
            case ALLOWED -> {
                // fall through
            }
        }

        return selection;
    }

    private boolean requirePermission(Player player) {
        if (player.hasPermission("nexusrealms.worldedit.use") || player.hasPermission("nexusrealms.worldedit.admin")) return true;
        player.sendMessage(ChatColor.RED + "No permission.");
        return false;
    }

    private Material parseMaterial(Player player, String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "\"" + name + "\" isn't a real material name.");
            return null;
        }
    }
}
