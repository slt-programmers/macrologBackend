package slt.util;

import slt.dto.FoodDto;
import slt.dto.MacroDto;

public class MacroUtils {

    private MacroUtils(){}

    public static Integer calculateCalories(MacroDto macro) {
        return Math.toIntExact(
                Math.round(
                        macro.getProtein() * 4 +
                                macro.getFat() * 9 +
                                macro.getCarbs() * 4));
    }

    public static Integer calculateCalories(FoodDto food) {
        return Math.toIntExact(
                Math.round(
                        food.getProtein() * 4 +
                                food.getFat() * 9 +
                                food.getCarbs() * 4));
    }

    public static MacroDto add(MacroDto one, MacroDto two) {
        MacroDto added = new MacroDto();
        added.setProtein(one.getProtein() + two.getProtein());
        added.setFat(one.getFat() + two.getFat());
        added.setCarbs(one.getCarbs() + two.getCarbs());
        added.setCalories(calculateCalories(added));
        return added;
    }

    public static MacroDto multiply(MacroDto macro, Double multiplier) {
        MacroDto multiplied = new MacroDto();
        multiplied.setProtein(macro.getProtein() * multiplier);
        multiplied.setFat(macro.getFat() * multiplier);
        multiplied.setCarbs(macro.getCarbs() * multiplier);
        multiplied.setCalories(calculateCalories(multiplied));

        return multiplied;
    }
}
