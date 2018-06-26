package csl.dto;

import java.util.HashMap;

public class FoodMacros {

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
}
