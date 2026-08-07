package com.nexusuniverse.realms;

import com.nexusuniverse.realms.land.ChunkUpgradeTier;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

    /** Every new team starts with this many chunks claimable, free, before any upgrade purchase. */
    public int landStartingChunks() {
        return Math.max(0, plugin.getConfig().getInt("land.starting-chunks", 10));
    }

    /**
     * The highest chunk-cap a team can reach by purchasing normal-tier upgrades (see
     * landUpgradeTiersNormal()). 0 means unlimited -- kept for backward compatibility with
     * servers that had this key at 0 before the upgrade system existed.
     */
    public int maxChunksPerTeam() {
        return Math.max(0, plugin.getConfig().getInt("land.max-chunks-per-team", 1000));
    }

    /** Same idea as maxChunksPerTeam(), but the higher ceiling available through op-tier pricing. */
    public int maxChunksPerTeamOp() {
        return Math.max(0, plugin.getConfig().getInt("land.max-chunks-per-team-op", 10000));
    }

    /** Safety cap on /realms admin claim's radius argument (in chunks) -- guards against an accidental huge number claiming (and disk-writing) an enormous area in one command. */
    public int landAdminBulkClaimMaxRadius() {
        return Math.max(0, plugin.getConfig().getInt("land.admin-bulk-claim-max-radius", 25));
    }

    /** The normal purchase ladder -- flat price to reach each chunk-cap tier, sorted ascending by chunk count. */
    public List<ChunkUpgradeTier> landUpgradeTiersNormal() {
        return parseTiers("land.upgrade-tiers");
    }

    /** The discounted, higher-ceiling ladder available to players holding nexusrealms.chunks.optier. */
    public List<ChunkUpgradeTier> landUpgradeTiersOp() {
        return parseTiers("land.upgrade-tiers-op");
    }

    private List<ChunkUpgradeTier> parseTiers(String path) {
        List<ChunkUpgradeTier> tiers = new ArrayList<>();
        List<Map<?, ?>> rows = plugin.getConfig().getMapList(path);
        for (Map<?, ?> row : rows) {
            Object chunksValue = row.get("chunks");
            Object priceValue = row.get("price");
            if (chunksValue == null || priceValue == null) continue;
            int chunks = ((Number) chunksValue).intValue();
            double price = ((Number) priceValue).doubleValue();
            tiers.add(new ChunkUpgradeTier(chunks, price));
        }
        tiers.sort(Comparator.comparingInt(ChunkUpgradeTier::chunks));
        return Collections.unmodifiableList(tiers);
    }

    // --- wilderness (unclaimed land) default rules ---

    public boolean wildernessAllowBuild() {
        return plugin.getConfig().getBoolean("wilderness.allow-build", true);
    }

    public boolean wildernessAllowContainers() {
        return plugin.getConfig().getBoolean("wilderness.allow-containers", true);
    }

    public boolean wildernessAllowDoors() {
        return plugin.getConfig().getBoolean("wilderness.allow-doors", true);
    }

    public boolean wildernessAllowPvp() {
        return plugin.getConfig().getBoolean("wilderness.allow-pvp", false);
    }

    public boolean wildernessAllowElytra() {
        return plugin.getConfig().getBoolean("wilderness.allow-elytra", true);
    }

    public void setWildernessSetting(String key, boolean value) {
        plugin.getConfig().set("wilderness." + key, value);
        plugin.saveConfig();
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
