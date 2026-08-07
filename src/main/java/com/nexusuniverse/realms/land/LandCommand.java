package com.nexusuniverse.realms.land;

import com.nexusuniverse.realms.RealmsConfig;
import com.nexusuniverse.realms.hints.HintManager;
import com.nexusuniverse.realms.team.Team;
import com.nexusuniverse.realms.team.TeamManager;
import com.nexusuniverse.realms.team.TeamRole;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LandCommand implements CommandExecutor {

    private final TeamManager teams;
    private final LandClaimManager land;
    private final RealmsConfig config;
    private final HintManager hints;

    public LandCommand(TeamManager teams, LandClaimManager land, RealmsConfig config, HintManager hints) {
        this.teams = teams;
        this.land = land;
        this.config = config;
        this.hints = hints;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /realms <claim|unclaim|info>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim" -> handleClaim(player);
            case "unclaim" -> handleUnclaim(player);
            case "info" -> handleInfo(player);
            default -> player.sendMessage(ChatColor.YELLOW + "Usage: /realms <claim|unclaim|info>");
        }
        return true;
    }

    private void handleClaim(Player player) {
        Team team = teams.teamOf(player.getUniqueId());
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You need to be in a team to claim land -- /team create <name> first.");
            return;
        }
        if (!team.hasRole(player.getUniqueId(), TeamRole.OFFICER)) {
            player.sendMessage(ChatColor.RED + "You need to be at least an officer to claim land for your team.");
            return;
        }
        if (land.chunkCountFor(team.id()) >= config.maxChunksPerTeam()) {
            player.sendMessage(ChatColor.RED + team.name() + " has hit its claim limit (" + config.maxChunksPerTeam() + " chunks).");
            return;
        }

        var chunk = player.getLocation().getChunk();
        if (!land.claim(chunk, team)) {
            player.sendMessage(ChatColor.RED + "This chunk is already claimed.");
            return;
        }
        player.sendMessage(ChatColor.AQUA + "Claimed this chunk for " + team.name() + ".");
        hints.sendContextual(player, "land_claim");
    }

    private void handleUnclaim(Player player) {
        Team team = teams.teamOf(player.getUniqueId());
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        if (!team.hasRole(player.getUniqueId(), TeamRole.OFFICER)) {
            player.sendMessage(ChatColor.RED + "You need to be at least an officer to unclaim land.");
            return;
        }
        var chunk = player.getLocation().getChunk();
        if (!land.unclaim(chunk, team)) {
            player.sendMessage(ChatColor.RED + "This chunk isn't claimed by your team.");
            return;
        }
        player.sendMessage(ChatColor.AQUA + "Unclaimed this chunk.");
    }

    private void handleInfo(Player player) {
        var chunk = player.getLocation().getChunk();
        java.util.UUID ownerId = land.ownerOf(chunk);
        if (ownerId == null) {
            player.sendMessage(ChatColor.GRAY + "This chunk is unclaimed.");
            return;
        }
        Team team = teams.byId(ownerId);
        player.sendMessage(ChatColor.GRAY + "This chunk belongs to " + ChatColor.WHITE
                + (team != null ? team.name() : "an unknown team") + ChatColor.GRAY + ".");
    }
}
