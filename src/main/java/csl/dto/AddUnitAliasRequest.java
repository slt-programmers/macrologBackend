package csl.dto;

public class AddUnitAliasRequest {

   String aliasName;
   Double aliasAmount;
   String aliasUnitName;

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public Double getAliasAmount() {
        return aliasAmount;
    }

    public void setAliasAmount(Double aliasAmount) {
        this.aliasAmount = aliasAmount;
    }

    public String getAliasUnitName() {
        return aliasUnitName;
    }

    public void setAliasUnitName(String aliasUnitName) {
        this.aliasUnitName = aliasUnitName;
    }
}
