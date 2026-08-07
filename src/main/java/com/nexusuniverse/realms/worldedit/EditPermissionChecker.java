package com.nexusuniverse.realms.worldedit;

import com.nexusuniverse.realms.land.PersonalClaim;
import com.nexusuniverse.realms.land.PersonalClaimManager;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * "Operators can use it like normal anywhere" / "players only within their claimed land
 * specifically attached to their name" -- this is the one class both halves of that split run
 * through, so the rule lives in exactly one place. nexusrealms.worldedit.admin bypasses the claim
 * check entirely (still subject to the volume cap in config, as a safety net against an
 * accidental huge selection); without it, EVERY corner of the selection has to fall inside a
 * SINGLE personal claim that player themselves owns -- not team territory generally, and not a
 * selection that straddles two of their own claims either, since "attached to their name" reads
 * as one specific property, not team-wide reach.
 */
public class EditPermissionChecker {

    private final PersonalClaimManager personalClaims;

    public EditPermissionChecker(PersonalClaimManager personalClaims) {
        this.personalClaims = personalClaims;
    }

    public enum Result { ALLOWED, NO_PERMISSION, NOT_YOUR_CLAIM }

    public Result check(Player player, Selection selection) {
        if (player.hasPermission("nexusrealms.worldedit.admin")) return Result.ALLOWED;
        if (!player.hasPermission("nexusrealms.worldedit.use")) return Result.NO_PERMISSION;

        List<PersonalClaim> owned = personalClaims.claimsOf(player.getUniqueId());
        if (owned.isEmpty()) return Result.NOT_YOUR_CLAIM;

        for (PersonalClaim claim : owned) {
            if (fullyContains(claim, selection)) return Result.ALLOWED;
        }
        return Result.NOT_YOUR_CLAIM;
    }

    /** True only if EVERY corner of the selection's bounding box is inside this one claim -- not just the center or one corner. */
    private boolean fullyContains(PersonalClaim claim, Selection selection) {
        String world = selection.world().getName();
        return claim.contains(world, selection.minX(), selection.minZ())
                && claim.contains(world, selection.minX(), selection.maxZ())
                && claim.contains(world, selection.maxX(), selection.minZ())
                && claim.contains(world, selection.maxX(), selection.maxZ());
    }
}
