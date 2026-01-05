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
        final var grams = portion != null ? portion.getGrams() : 100;
        final var calculatedMacros =  MacroDto.builder()
                .protein(food.getProtein() / 100 * grams * multiplier)
                .fat(food.getFat() / 100 * grams * multiplier)
                .carbs(food.getCarbs() / 100 * grams * multiplier)
                .build();
        calculateCalories(calculatedMacros);
        return calculatedMacros;
    }

    public static MacroDto calculateMacro(final FoodDto foodDto, final PortionDto portionDto) {
        final var calculatedMacros =  MacroDto.builder()
                .protein(foodDto.getProtein() / 100 * portionDto.getGrams())
                .fat(foodDto.getFat() / 100 * portionDto.getGrams())
                .carbs(foodDto.getCarbs() / 100 * portionDto.getGrams())
                .build();
        calculateCalories(calculatedMacros);
        return calculatedMacros;
    }

    public static MacroDto add(final MacroDto one, final MacroDto two) {
        return MacroDto.builder()
                .protein(one.getProtein() + two.getProtein())
                .fat(one.getFat() + two.getFat())
                .carbs(one.getCarbs() + two.getCarbs())
                .calories(one.getCalories() + two.getCalories())
                .build();
    }

    private static void calculateCalories(final MacroDto macro) {
        final var calories = Math.toIntExact(
                Math.round(
                        macro.getProtein() * 4 +
                                macro.getFat() * 9 +
                                macro.getCarbs() * 4));
        macro.setCalories(calories);
    }
}
