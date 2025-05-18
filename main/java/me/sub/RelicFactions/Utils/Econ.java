package me.sub.RelicFactions.Utils;

import me.sub.RelicFactions.Files.Classes.User;
import me.sub.RelicFactions.Main.Main;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.List;

public class Econ implements Economy {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double v) {
        return Calculate.formatMoney(v);
    }

    @Override
    public String currencyNamePlural() {
        return "";
    }

    @Override
    public String currencyNameSingular() {
        return "";
    }

    @Override
    public boolean hasAccount(String s) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }

    @Override
    public double getBalance(String s) {
        User user = User.get(s);
        if (user == null) return 0;
        return user.getBalance().doubleValue();
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        User user = User.get(offlinePlayer);
        if (user == null) return 0;
        return user.getBalance().doubleValue();
    }

    @Override
    public double getBalance(String s, String s1) {
        User user = User.get(s);
        if (user == null) return 0;
        return user.getBalance().doubleValue();
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        User user = User.get(offlinePlayer);
        if (user == null) return 0;
        return user.getBalance().doubleValue();
    }

    @Override
    public boolean has(String s, double v) {
        User user = User.get(s);
        if (user == null) return false;
        return user.getBalance().doubleValue() >= v;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double v) {
        User user = User.get(offlinePlayer);
        if (user == null) return false;
        return user.getBalance().doubleValue() >= v;
    }

    @Override
    public boolean has(String s, String s1, double v) {
        User user = User.get(s);
        if (user == null) return false;
        return user.getBalance().doubleValue() >= v;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        User user = User.get(offlinePlayer);
        if (user == null) return false;
        return user.getBalance().doubleValue() >= v;
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        User user = User.get(s);
        if (user == null) return new EconomyResponse(v, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist.");
        user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(v)));
        return new EconomyResponse(v, user.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "Successfully withdrew");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
        User user = User.get(offlinePlayer);
        if (user == null) return new EconomyResponse(v, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist.");
        user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(v)));
        return new EconomyResponse(v, user.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "Successfully withdrew");
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        User user = User.get(s);
        if (user == null) return new EconomyResponse(v, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist.");
        user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(v)));
        return new EconomyResponse(v, user.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "Successfully withdrew");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        User user = User.get(offlinePlayer);
        if (user == null) return new EconomyResponse(v, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist.");
        user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(v)));
        return new EconomyResponse(v, user.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "Successfully withdrew");
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        User user = User.get(s);
        if (user == null) return new EconomyResponse(v, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist.");
        user.setBalance(user.getBalance().add(BigDecimal.valueOf(v)));
        return new EconomyResponse(v, user.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "Successfully deposited");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        User user = User.get(offlinePlayer);
        if (user == null) return new EconomyResponse(v, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist.");
        user.setBalance(user.getBalance().add(BigDecimal.valueOf(v)));
        return new EconomyResponse(v, user.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "Successfully deposited");
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        User user = User.get(s);
        if (user == null) return new EconomyResponse(v, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist.");
        user.setBalance(user.getBalance().add(BigDecimal.valueOf(v)));
        return new EconomyResponse(v, user.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "Successfully deposited");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        User user = User.get(offlinePlayer);
        if (user == null) return new EconomyResponse(v, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist.");
        user.setBalance(user.getBalance().add(BigDecimal.valueOf(v)));
        return new EconomyResponse(v, user.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "Successfully deposited");
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }
}
