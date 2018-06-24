package csl.dto;

import java.util.HashMap;

public class FoodMacros {

    private String name;
    private HashMap<String,Macro> macroPerUnit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Macro> getMacroPerUnit() {
        if (this.macroPerUnit == null){
            this.macroPerUnit = new HashMap<>();
        }
        return macroPerUnit;
    }

    public void setMacroPerUnit(HashMap<String, Macro> macroPerUnit) {
        this.macroPerUnit = macroPerUnit;
    }

    public void addMacroPerUnit(String unit, Macro macro) {

       getMacroPerUnit().put(unit,macro);
    }
}
