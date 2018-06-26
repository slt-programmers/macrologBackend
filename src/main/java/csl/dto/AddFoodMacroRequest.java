package csl.dto;

public class AddFoodMacroRequest {

    private String name;
    Double defaultAmount;
    String defaultUnitname;

    private Macro macroPerUnit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getDefaultAmount() {
        return defaultAmount;
    }

    public void setDefaultAmount(Double defaultAmount) {
        this.defaultAmount = defaultAmount;
    }

    public String getDefaultUnitname() {
        return defaultUnitname;
    }

    public void setDefaultUnitname(String defaultUnitname) {
        this.defaultUnitname = defaultUnitname;
    }

    public Macro getMacroPerUnit() {
        return macroPerUnit;
    }

    public void setMacroPerUnit(Macro macroPerUnit) {
        this.macroPerUnit = macroPerUnit;
    }
}
