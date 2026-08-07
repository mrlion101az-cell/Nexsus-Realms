package com.nexusuniverse.realms;

import org.bukkit.plugin.java.JavaPlugin;

public class RealmsConfig {

    private final JavaPlugin plugin;

    public RealmsConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        // saveDefaultConfig() only writes config.yml the very first time this plugin is
        // installed -- copyDefaults(true) + saveConfig() merges in anything a later update adds
        // to an already-existing config.yml, instead of it silently never showing up.
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    // --- homes ---

    /** Falls back to this if the player doesn't hold nexusrealms.homes.admin. */
    public int homesMaxDefault() {
        return Math.max(0, plugin.getConfig().getInt("homes.max-default", 1));
    }

    public int homesMaxAdmin() {
        return Math.max(0, plugin.getConfig().getInt("homes.max-admin", 5));
    }

    // --- country-level (chunk) claiming ---

    /** Chunks a team can claim in total -- 0 means unlimited. */
    public int maxChunksPerTeam() {
        return Math.max(0, plugin.getConfig().getInt("land.max-chunks-per-team", 0));
    }

    // --- personal claims (a trusted member's small claim inside their own team's territory) ---

    public int personalClaimRadius() {
        return Math.max(1, plugin.getConfig().getInt("land.personal-claim.radius", 15));
    }

    public int personalClaimMaxPerMember() {
        return Math.max(0, plugin.getConfig().getInt("land.personal-claim.max-per-member", 2));
    }

    // --- worldedit-inspired region editing ---

    public org.bukkit.Material worldEditWandMaterial() {
        String name = plugin.getConfig().getString("worldedit.wand-material", "GOLDEN_AXE");
        try {
            return org.bukkit.Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return org.bukkit.Material.GOLDEN_AXE;
        }
    }

    /** Applies to every /redit operation, admin or not -- a safety net against one accidental huge selection freezing the server. */
    public long worldEditMaxVolume() {
        return Math.max(1, plugin.getConfig().getLong("worldedit.max-volume", 250_000));
    }

    public int worldEditUndoDepth() {
        return Math.max(1, plugin.getConfig().getInt("worldedit.undo-depth", 5));
    }
}
