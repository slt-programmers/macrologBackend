package csl.dto;

import java.util.HashMap;

public class FoodMacros {

    private Long foodId;
    private String name;
    private String amountUnit;
    private HashMap<Double,Macro> macroPerUnit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<Double, Macro> getMacroPerUnit() {
        if (this.macroPerUnit == null){
            this.macroPerUnit = new HashMap<>();
        }
        return macroPerUnit;
    }

    public void setMacroPerUnit(HashMap<Double, Macro> macroPerUnit) {
        this.macroPerUnit = macroPerUnit;
    }

    public void addMacroPerUnit(Double unit, Macro macro) {

       getMacroPerUnit().put(unit,macro);
    }

    public String getAmountUnit() {
        return amountUnit;
    }

    public void setAmountUnit(String amountUnit) {
        this.amountUnit = amountUnit;
    }

    public String toString(){
        return "[foodId="+foodId+",name "+name+"]";
    }

    public void setFoodId(Long id) {
        this.foodId=id;
    }

    public Long getFoodId() {
        return foodId;
    }
}
