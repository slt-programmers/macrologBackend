package slt.servicetests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import slt.dto.FoodDto;
import slt.dto.PortionDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.servicetests.utils.AbstractApplicationIntegrationTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FoodControllerITest extends AbstractApplicationIntegrationTest {

    private Long userId;

    @BeforeAll
    public synchronized void setUserContext() {
        log.debug("Starting with userId" + this.userId);
        if (this.userId == null) {
            log.debug("Creating test user for test " + this.getClass().getName());
            this.userId = createUser(this.getClass().getName());
        }
        log.debug("Ending with userId" + this.userId);
        final var userInfo = new UserInfo();
        userInfo.setUserId(this.userId);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testFoodNoPortion() {
        FoodDto foodRequestZonderPortions = FoodDto.builder().name("foodNoPortion").carbs(1.0).fat(2.0).protein(3.0).build();
        final var responseEntity = foodController.postFood(foodRequestZonderPortions);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var allFoodEntity = foodController.getAllFood();
        assertThat(allFoodEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<FoodDto> foodDtos = allFoodEntity.getBody();
        Assertions.assertNotNull(foodDtos);
        FoodDto savedFood = foodDtos.stream().filter(f -> f.getName().equals("foodNoPortion")).findFirst().get();

        assertThat(savedFood.getName()).isEqualTo("foodNoPortion");
        assertThat(savedFood.getCarbs()).isEqualTo(1.0);
        assertThat(savedFood.getFat()).isEqualTo(2.0);
        assertThat(savedFood.getProtein()).isEqualTo(3.0);
        assertThat(savedFood.getPortions()).isEmpty();
    }

    @Test
    public void testFoodAlreadyExists() {
        FoodDto foodOriginal = FoodDto.builder().name("foodDuplicate").carbs(1.0).fat(2.0).protein(3.0).build();
        var responseEntity = foodController.postFood(foodOriginal);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        FoodDto foodDuplicate = FoodDto.builder().name("foodDuplicate").carbs(1.0).fat(2.0).protein(3.0).build();
        responseEntity = foodController.postFood(foodDuplicate);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testFoodMultiplePortion() {
        FoodDto foodRequestMetPortions = FoodDto.builder()
                .name("foodMultiplePortion")
                .carbs(1.0)
                .fat(2.0)
                .protein(3.0)
                .portions(Arrays.asList(
                                PortionDto.builder()
                                        .description("portion1")
                                        .grams(200.0)
                                        .build(),
                                PortionDto.builder()
                                        .description("portion2")
                                        .grams(300.0)
                                        .build()
                        )
                )
                .build();
        final var responseEntity = foodController.postFood(foodRequestMetPortions);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var allFoodEntity = foodController.getAllFood();
        assertThat(allFoodEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<FoodDto> foodDtos =  allFoodEntity.getBody();

        Assertions.assertNotNull(foodDtos);
        FoodDto savedFood = foodDtos.stream().filter(f -> f.getName().equals("foodMultiplePortion")).findFirst().get();

        assertThat(savedFood.getName()).isEqualTo("foodMultiplePortion");
        assertThat(savedFood.getCarbs()).isEqualTo(1.0);
        assertThat(savedFood.getFat()).isEqualTo(2.0);
        assertThat(savedFood.getProtein()).isEqualTo(3.0);
        assertThat(savedFood.getPortions()).hasSize(2);

        PortionDto portion1 = savedFood.getPortions().stream().filter(p -> p.getDescription().equals("portion1")).findFirst().get();
        assertThat(portion1.getGrams()).isEqualTo(200.0);
        assertThat(portion1.getId()).isNotNull();
        assertThat(portion1.getMacros()).isNotNull();

        PortionDto portion2 = savedFood.getPortions().stream().filter(p -> p.getDescription().equals("portion2")).findFirst().get();
        assertThat(portion2.getGrams()).isEqualTo(300.0);
        assertThat(portion2.getId()).isNotNull();
        assertThat(portion2.getMacros()).isNotNull();
    }

    @Test
    public void testFoodGetById() {
        FoodDto foodRequestZonderPortions = FoodDto.builder()
                .name("foodById")
                .carbs(1.0)
                .fat(2.0)
                .protein(3.0)
                .portions(Arrays.asList(
                                PortionDto.builder()
                                        .description("portion3")
                                        .grams(200.0)
                                        .build(),
                                PortionDto.builder()
                                        .description("portion4")
                                        .grams(300.0)
                                        .build()
                        )
                )
                .build();
        final var responseEntity = foodController.postFood(foodRequestZonderPortions);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var allFoodEntity = foodController.getAllFood();
        assertThat(allFoodEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<FoodDto> foodDtos = allFoodEntity.getBody();

        Assertions.assertNotNull(foodDtos);
        FoodDto savedFood = foodDtos.stream().filter(f -> f.getName().equals("foodById")).findFirst().get();

        final var foodByIDEntityNotFound = foodController.getFoodById(666L);
        assertThat(foodByIDEntityNotFound.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        final var foodByIDEntity = foodController.getFoodById(savedFood.getId());
        savedFood = foodByIDEntity.getBody();

        Assertions.assertNotNull(savedFood);
        assertThat(savedFood.getName()).isEqualTo("foodById");
        assertThat(savedFood.getCarbs()).isEqualTo(1.0);
        assertThat(savedFood.getFat()).isEqualTo(2.0);
        assertThat(savedFood.getProtein()).isEqualTo(3.0);
        assertThat(savedFood.getPortions()).hasSize(2);

        PortionDto portion1 = savedFood.getPortions().stream().filter(p -> p.getDescription().equals("portion3")).findFirst().get();
        assertThat(portion1.getGrams()).isEqualTo(200.0);
        assertThat(portion1.getId()).isNotNull();
        assertThat(portion1.getMacros()).isNotNull();

        PortionDto portion2 = savedFood.getPortions().stream().filter(p -> p.getDescription().equals("portion4")).findFirst().get();
        assertThat(portion2.getGrams()).isEqualTo(300.0);
        assertThat(portion2.getId()).isNotNull();
        assertThat(portion2.getMacros()).isNotNull();

    }

    @Test
    public void testFoodAddPortion() {
        final var foodDtoZonderPortions = FoodDto.builder().name("foodAddPortion").carbs(1.0).fat(2.0).protein(3.0).build();
        final var responseEntity = foodController.postFood(foodDtoZonderPortions);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        var allFoodEntity = foodController.getAllFood();
        assertThat(allFoodEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        var foodDtos = allFoodEntity.getBody();
        Assertions.assertNotNull(foodDtos);
        final var optionalSavedFood = foodDtos.stream().filter(f -> f.getName().equals("foodAddPortion")).findFirst();
        Assertions.assertTrue(optionalSavedFood.isPresent());
        final var savedFood = optionalSavedFood.get();

        final var foodDtoWithPortion = FoodDto.builder()
                .id(savedFood.getId())
                .carbs(20.0)
                .fat(30.0)
                .protein(40.0)
                .name("newName")
                .portions(Collections.singletonList(PortionDto.builder()
                        .description("newPortion")
                        .grams(2.0)
                        .build()))
                .build();
        foodController.postFood(foodDtoWithPortion);

        allFoodEntity = foodController.getAllFood();
        foodDtos = allFoodEntity.getBody();
        Assertions.assertNotNull(foodDtos);
        final var originalFood = foodDtos.stream().filter(f -> f.getName().equals("foodAddPortion")).findFirst();
        assertThat(originalFood.isPresent()).isFalse();

        var optionalAlteredFood = foodDtos.stream().filter(f -> f.getName().equals("newName")).findFirst();
        Assertions.assertTrue(optionalAlteredFood.isPresent());
        var alteredFood = optionalAlteredFood.get();

        assertThat(alteredFood.getName()).isEqualTo("newName");
        assertThat(alteredFood.getCarbs()).isEqualTo(20.0);
        assertThat(alteredFood.getFat()).isEqualTo(30.0);
        assertThat(alteredFood.getProtein()).isEqualTo(40.0);
        assertThat(alteredFood.getPortions()).hasSize(1);

        final var optionalPortion2 = alteredFood.getPortions().stream().filter(p -> p.getDescription().equals("newPortion")).findFirst();
        Assertions.assertTrue(optionalPortion2.isPresent());
        final var portion2 = optionalPortion2.get();
        assertThat(portion2.getGrams()).isEqualTo(2.0);
        assertThat(portion2.getId()).isNotNull();
        assertThat(portion2.getMacros()).isNotNull();

        // Alter the portion
        final var foodDtoWithAlteredPortion = FoodDto.builder()
                .id(savedFood.getId())
                .carbs(20.0)
                .fat(30.0)
                .protein(40.0)
                .name("newName")
                .portions(Collections.singletonList(PortionDto.builder()
                        .description("newPortionName")
                        .grams(3.0)
                        .id(portion2.getId())
                        .build()))
                .build();
        foodController.postFood(foodDtoWithAlteredPortion);

        allFoodEntity = foodController.getAllFood();
        foodDtos = allFoodEntity.getBody();

        Assertions.assertNotNull(foodDtos);
        optionalAlteredFood = foodDtos.stream().filter(f -> f.getName().equals("newName")).findFirst();
        Assertions.assertTrue(optionalAlteredFood.isPresent());
        alteredFood = optionalAlteredFood.get();

        assertThat(alteredFood.getName()).isEqualTo("newName");
        assertThat(alteredFood.getCarbs()).isEqualTo(20.0);
        assertThat(alteredFood.getFat()).isEqualTo(30.0);
        assertThat(alteredFood.getProtein()).isEqualTo(40.0);
        assertThat(alteredFood.getPortions()).hasSize(1);

        final var optionalPortionAltered = alteredFood.getPortions().stream().filter(p -> p.getDescription().equals("newPortionName")).findFirst();
        Assertions.assertTrue(optionalPortionAltered.isPresent());
        final var portionAltered = optionalPortionAltered.get();
        assertThat(portionAltered.getGrams()).isEqualTo(3.0);
        assertThat(portionAltered.getId()).isNotNull();
        assertThat(portionAltered.getMacros()).isNotNull();

    }


}
