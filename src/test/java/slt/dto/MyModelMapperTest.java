package slt.dto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import slt.database.FoodRepository;
import slt.database.PortionRepository;
import slt.database.entities.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RunWith(MockitoJUnitRunner.class)
class MyModelMapperTest {

    @Mock
    FoodRepository foodRepository;

    @Mock
    PortionRepository portionRepository;

    @InjectMocks
    MyModelMapper mapper ;

    @BeforeAll
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLocalDateSQLMapping() {

        LocalDate input = LocalDate.parse("2013-01-02");
        Date dateConverted = mapper.getConfiguredMapper().map(input, Date.class);
        LocalDate dateConvertedBack = mapper.getConfiguredMapper().map(dateConverted, LocalDate.class);
        assertThat(dateConvertedBack).isEqualTo(input);
    }

    @Test
    public void testWeightMapping() {

        WeightDto weightDto = WeightDto.builder()
                .weight(20.0)
                .day(LocalDate.parse("2010-04-01"))
                .remark("remark")
                .build();

        Weight mappedWeight = mapper.getConfiguredMapper().map(weightDto, Weight.class);
        mapper.getConfiguredMapper().validate();

        assertThat(mappedWeight.getRemark()).isEqualTo(weightDto.getRemark());
        assertThat(mappedWeight.getDay()).isInSameDayAs(Date.valueOf(weightDto.getDay()));
        assertThat(mappedWeight.getValue()).isEqualTo(weightDto.getWeight());

        // and back to original
        WeightDto mappedBack = mapper.getConfiguredMapper().map(mappedWeight, WeightDto.class);
        mapper.getConfiguredMapper().validate();

        assertThat(mappedBack.getWeight()).isEqualTo(weightDto.getWeight());
        assertThat(mappedBack.getRemark()).isEqualTo(weightDto.getRemark());
        assertThat(mappedBack.getDay()).isEqualTo(weightDto.getDay());

    }

    @Test
    public void testActivityMapping() {
        LocalDate localDate = LocalDate.parse("2010-01-02");
        java.util.Date day = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LogActivityDto dto = LogActivityDto.builder()
                .calories(1.0)
                .day(day)
                .name("running").build();

        LogActivity mapped = mapper.getConfiguredMapper().map(dto, LogActivity.class);
        mapper.getConfiguredMapper().validate();

        assertThat(mapped.getDay().toLocalDate()).isEqualTo(localDate);
        assertThat(mapped.getName()).isEqualTo(dto.getName());
        assertThat(mapped.getCalories()).isEqualTo(dto.getCalories());

        LogActivity mappedBack = mapper.getConfiguredMapper().map(mapped, LogActivity.class);
        mapper.getConfiguredMapper().validate();

        assertThat(mappedBack.getDay().toLocalDate()).isEqualTo(localDate);
        assertThat(mappedBack.getName()).isEqualTo(dto.getName());
        assertThat(mappedBack.getCalories()).isEqualTo(dto.getCalories());
    }

    @Test
    public void testAddMealRequestMapping() {
        AddMealRequest dto = AddMealRequest.builder()
                .name("meal")
                .ingredients(Arrays.asList(
                        AddMealIngredientDto.builder()
                                .foodId(1l)
                                .portionId(1l)
                                .multiplier(2.0).build()
                )).build();

        Meal mapped = mapper.getConfiguredMapper().map(dto, Meal.class);
        mapper.getConfiguredMapper().validate();

        assertThat(mapped.getName()).isEqualTo("meal");
        assertThat(mapped.getIngredients()).hasSize(1);

        assertThat(mapped.getIngredients().get(0).getMeal()).isNotNull();
        assertThat(mapped.getIngredients().get(0).getFoodId()).isEqualTo(dto.getIngredients().get(0).getFoodId());
        assertThat(mapped.getIngredients().get(0).getMultiplier()).isEqualTo(dto.getIngredients().get(0).getMultiplier());
        assertThat(mapped.getIngredients().get(0).getPortionId()).isEqualTo(dto.getIngredients().get(0).getPortionId());
    }

    @Test
    public void testMealMapping() {
        MealDto dto = MealDto.builder()
                .name("nameMeal")
                .id(1l)
                .ingredients(Arrays.asList(
                        IngredientDto.builder()
                                .id(20l)
                                .multiplier(3.0)
                                .portionId(2l)
                                .food(
                                        FoodDto.builder()
                                                .id(30l)
                                                .carbs(2.0)
                                                .fat(3.0)
                                                .protein(4.0)
                                                .portions(Arrays.asList(
                                                        PortionDto.builder()
                                                                .id(2l)
                                                                .grams(30.0)
                                                                .description("portion")
                                                                .build())
                                                ).build()
                                ).build()
                )).build();


        Food foodEntity = Food.builder().id(2l).build();
        Mockito.when(foodRepository.getFoodById(isNull(),eq(2l))).thenReturn(foodEntity);
        Meal mapped = mapper.getConfiguredMapper().map(dto, Meal.class);
        mapper.getConfiguredMapper().validate();

        assertThat(mapped.getName()).isEqualTo(dto.getName());
        assertThat(mapped.getIngredients()).isNotEmpty();
        assertThat(mapped.getIngredients()).hasSize(1);
        Ingredient ingredientMapped = mapped.getIngredients().get(0);
        assertThat(ingredientMapped.getPortionId()).isEqualTo(2l);
        assertThat(ingredientMapped.getFoodId()).isEqualTo(30l);
        assertThat(ingredientMapped.getMultiplier()).isEqualTo(3.0);
        assertThat(ingredientMapped.getMeal()).isEqualTo(mapped);

        // UserId needs to be set
        mapped.setUserId(2);
        Mockito.when(foodRepository.getFoodById(eq(2), eq(30l))).thenReturn(Food.builder().build());
        Mockito.when(portionRepository.getPortions(eq(30l))).thenReturn(Arrays.asList(
                Portion.builder().id(2l).build()
        ));

        MealDto mappedBack = mapper.getConfiguredMapper().map(mapped, MealDto.class);
        mapper.getConfiguredMapper().validate();

        assertThat(mappedBack.getName()).isEqualTo(dto.getName());
        assertThat(mappedBack.getIngredients()).isNotEmpty();
        assertThat(mappedBack.getIngredients()).hasSize(1);
        Ingredient ingredientMappedBack = mapped.getIngredients().get(0);
        assertThat(ingredientMappedBack.getPortionId()).isEqualTo(2l);
        assertThat(ingredientMappedBack.getFoodId()).isEqualTo(30l);
        assertThat(ingredientMappedBack.getMultiplier()).isEqualTo(3.0);
        assertThat(ingredientMappedBack.getMeal()).isEqualTo(mapped);

    }
}