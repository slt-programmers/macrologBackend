package csl.dto;

public class FoodAlias {

    String aliasName;
    Double amountNumber;
    String amountUnit;

    Double aliasProtein;
    Double aliasCarbs;
    Double aliasFat;

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public Double getAmountNumber() {
        return amountNumber;
    }

    public void setAmountNumber(Double amountNumber) {
        this.amountNumber = amountNumber;
    }

    public String getAmountUnit() {
        return amountUnit;
    }

    public void setAmountUnit(String amountUnit) {
        this.amountUnit = amountUnit;
    }

    public Double getAliasProtein() {
        return aliasProtein;
    }

    public void setAliasProtein(Double aliasProtein) {
        this.aliasProtein = aliasProtein;
    }

    public Double getAliasCarbs() {
        return aliasCarbs;
    }

    public void setAliasCarbs(Double aliasCarbs) {
        this.aliasCarbs = aliasCarbs;
    }

    public Double getAliasFat() {
        return aliasFat;
    }

    public void setAliasFat(Double aliasFat) {
        this.aliasFat = aliasFat;
    }
}
