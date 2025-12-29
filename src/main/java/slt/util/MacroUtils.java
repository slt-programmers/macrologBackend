package slt.util;

import lombok.experimental.UtilityClass;
import slt.database.entities.Food;
import slt.database.entities.Portion;
import slt.dto.FoodDto;
import slt.dto.MacroDto;
import slt.dto.PortionDto;

@UtilityClass
public class MacroUtils {

    public static MacroDto calculateMacro(final Food food, final Portion portion) {
        final var calculatedMacros = new MacroDto();
        final var grams = portion != null ? portion.getGrams() : 100;
        calculatedMacros.setCarbs(food.getCarbs() / 100 * grams);
        calculatedMacros.setProtein(food.getProtein() / 100 * grams);
        calculatedMacros.setFat(food.getFat() / 100 * grams);
        return calculatedMacros;
    }

    public static MacroDto calculateMacro(final FoodDto food, final PortionDto portion) {
        final var calculatedMacros = new MacroDto();
        calculatedMacros.setCarbs(food.getCarbs() / 100 * portion.getGrams());
        calculatedMacros.setProtein(food.getProtein() / 100 * portion.getGrams());
        calculatedMacros.setFat(food.getFat() / 100 * portion.getGrams());
        return calculatedMacros;
    }

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
