package csl.database.model;

/**
 * Created by Carmen on 18-3-2018.
 */
public class FoodAlias {

    private String aliasname;
    private Double amountNumber;
    private String amountUnit;

    public FoodAlias() {
    }

    public FoodAlias(String aliasname,
                     Double amountNumber,
                     String amountUnit) {
        this.aliasname = aliasname;
        this.amountNumber = amountNumber;
        this.amountUnit = amountUnit;

    }

    public String getAliasname() {
        return aliasname;
    }

    public void setAliasname(String aliasname) {
        this.aliasname = aliasname;
    }

    public String getAmountUnit() {
        return amountUnit;
    }

    public void setAmountUnit(String amountUnit) {
        this.amountUnit = amountUnit;
    }

    public Double getAmountNumber() {
        return amountNumber;
    }

    public void setAmountNumber(Double amountNumber) {
        this.amountNumber = amountNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FoodAlias food = (FoodAlias) o;

        if (!aliasname.equals(food.aliasname)) return false;
        if (!amountUnit.equals(food.amountUnit)) return false;
        return amountNumber.equals(food.amountNumber);
    }

    @Override
    public int hashCode() {
        int result = aliasname.hashCode();
        result = 31 * result + amountUnit.hashCode();
        result = 31 * result + amountNumber.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FoodAlias{" +
                "aliasname='" + aliasname + '\'' +
                '}';
    }

    public void setFoodId(Long foodId) {

    }
}
