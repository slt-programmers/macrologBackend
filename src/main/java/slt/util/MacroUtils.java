package slt.util;

import lombok.experimental.UtilityClass;
import slt.database.entities.Food;
import slt.database.entities.Portion;
import slt.dto.FoodDto;
import slt.dto.MacroDto;
import slt.dto.PortionDto;

@UtilityClass
public class MacroUtils {

    public static MacroDto calculateMacro(final Food food, final Portion portion, final Double multiplier) {
        final var calculatedMacros = new MacroDto();
        final var grams = portion != null ? portion.getGrams() : 100;
        calculatedMacros.setCarbs(food.getCarbs() / 100 * grams * multiplier);
        calculatedMacros.setProtein(food.getProtein() / 100 * grams * multiplier);
        calculatedMacros.setFat(food.getFat() / 100 * grams * multiplier);
        calculatedMacros.setCalories(calculateCalories(calculatedMacros));
        return calculatedMacros;
    }

    public static MacroDto calculateMacro(final FoodDto foodDto, final PortionDto portionDto) {
        final var calculatedMacros = new MacroDto();
        calculatedMacros.setCarbs(foodDto.getCarbs() / 100 * portionDto.getGrams());
        calculatedMacros.setProtein(foodDto.getProtein() / 100 * portionDto.getGrams());
        calculatedMacros.setFat(foodDto.getFat() / 100 * portionDto.getGrams());
        return calculatedMacros;
    }

    public static MacroDto add(final MacroDto one, final MacroDto two) {
        final var added = new MacroDto();
        added.setProtein(one.getProtein() + two.getProtein());
        added.setFat(one.getFat() + two.getFat());
        added.setCarbs(one.getCarbs() + two.getCarbs());
        added.setCalories(calculateCalories(added));
        return added;
    }

    private static Integer calculateCalories(MacroDto macro) {
        return Math.toIntExact(
                Math.round(
                        macro.getProtein() * 4 +
                                macro.getFat() * 9 +
                                macro.getCarbs() * 4));
    }
}
