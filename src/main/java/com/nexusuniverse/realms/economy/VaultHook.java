package com.nexusuniverse.realms.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Thin wrapper around the Vault Economy service, used for the chunk-cap upgrade purchases
 * (see LandCommand's /realms upgrade). If Vault isn't present, every method degrades to a safe
 * failure rather than throwing, so a purchase attempt just gets rejected with a clear message.
 */
public class VaultHook {

    private Economy economy;

    public boolean setup(JavaPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer()
                .getServicesManager()
                .getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        this.economy = rsp.getProvider();
        return true;
    }

    public boolean isReady() {
        return economy != null;
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!isReady()) return false;
        if (economy.getBalance(player) < amount) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public String format(double amount) {
        if (isReady()) {
            return economy.format(amount);
        }
        return String.format("%.2f", amount);
    }
}
