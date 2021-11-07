package slt.servicetests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import slt.dto.FoodRequest;
import slt.dto.FoodDto;
import slt.dto.PortionDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.servicetests.utils.AbstractApplicationIntegrationTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FoodServiceITest extends AbstractApplicationIntegrationTest {

    private Integer userId;

    @BeforeAll
    public synchronized void setUserContext() {

        log.debug("Starting with userId" + this.userId);
        if (this.userId == null) {
            log.debug("Creating test user for test " + this.getClass().getName());
            this.userId = createUser(this.getClass().getName());
        }
        log.debug("Ending with userId" + this.userId);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Integer.valueOf(this.userId));
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testFoodNoPortion() {

        FoodRequest foodRequestZonderPortions = FoodRequest.builder().name("foodNoPortion").carbs(1.0).fat(2.0).protein(3.0).build();
        ResponseEntity responseEntity = foodService.addFood(foodRequestZonderPortions);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

        ResponseEntity allFoodEntity = foodService.getAllFood();
        assertThat(allFoodEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        List<FoodDto> foodDtos = (List<FoodDto>) allFoodEntity.getBody();
        FoodDto savedFood = foodDtos.stream().filter(f -> f.getName().equals("foodNoPortion")).findFirst().get();

        assertThat(savedFood.getName()).isEqualTo("foodNoPortion");
        assertThat(savedFood.getCarbs()).isEqualTo(1.0);
        assertThat(savedFood.getFat()).isEqualTo(2.0);
        assertThat(savedFood.getProtein()).isEqualTo(3.0);
        assertThat(savedFood.getPortions()).isEmpty();
    }

    @Test
    public void testFoodAlreadyExists() {

        FoodRequest foodOriginal = FoodRequest.builder().name("foodDuplicate").carbs(1.0).fat(2.0).protein(3.0).build();
        ResponseEntity responseEntity = foodService.addFood(foodOriginal);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

        FoodRequest foodDuplicate = FoodRequest.builder().name("foodDuplicate").carbs(1.0).fat(2.0).protein(3.0).build();
        responseEntity = foodService.addFood(foodDuplicate);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void testFoodMultiplePortion() {

        FoodRequest foodRequestMetPortions = FoodRequest.builder()
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
        ResponseEntity responseEntity = foodService.addFood(foodRequestMetPortions);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

        ResponseEntity allFoodEntity = foodService.getAllFood();
        assertThat(allFoodEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        List<FoodDto> foodDtos = (List<FoodDto>) allFoodEntity.getBody();

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

        FoodRequest foodRequestZonderPortions = FoodRequest.builder()
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
        ResponseEntity responseEntity = foodService.addFood(foodRequestZonderPortions);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

        ResponseEntity allFoodEntity = foodService.getAllFood();
        assertThat(allFoodEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        List<FoodDto> foodDtos = (List<FoodDto>) allFoodEntity.getBody();

        FoodDto savedFood = foodDtos.stream().filter(f -> f.getName().equals("foodById")).findFirst().get();

        ResponseEntity foodByIDEntityNotFound = foodService.getFoodInformation(666l);
        assertThat(foodByIDEntityNotFound.getStatusCodeValue()).isEqualTo(HttpStatus.NO_CONTENT.value());

        ResponseEntity foodByIDEntity = foodService.getFoodInformation(savedFood.getId());
        savedFood = (FoodDto) foodByIDEntity.getBody();

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

        FoodRequest foodRequestZonderPortions = FoodRequest.builder().name("foodAddPortion").carbs(1.0).fat(2.0).protein(3.0).build();
        ResponseEntity responseEntity = foodService.addFood(foodRequestZonderPortions);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

        ResponseEntity allFoodEntity = foodService.getAllFood();
        assertThat(allFoodEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        List<FoodDto> foodDtos = (List<FoodDto>) allFoodEntity.getBody();
        FoodDto savedFood = foodDtos.stream().filter(f -> f.getName().equals("foodAddPortion")).findFirst().get();

        FoodRequest foodWithAddedPortionRequest = FoodRequest.builder()
                .id(savedFood.getId())
                .carbs(20.0)
                .fat(30.0)
                .protein(40.0)
                .name("newName")
                .portions(Arrays.asList(PortionDto.builder()
                        .description("newPortion")
                        .grams(2.0)
                        .build()))
                .build();
        foodService.addFood(foodWithAddedPortionRequest);

        allFoodEntity = foodService.getAllFood();
        foodDtos = (List<FoodDto>) allFoodEntity.getBody();
        Optional<FoodDto> originalFood = foodDtos.stream().filter(f -> f.getName().equals("foodAddPortion")).findFirst();
        assertThat(originalFood.isPresent()).isFalse();

        FoodDto alteredFood = foodDtos.stream().filter(f -> f.getName().equals("newName")).findFirst().get();

        assertThat(alteredFood.getName()).isEqualTo("newName");
        assertThat(alteredFood.getCarbs()).isEqualTo(20.0);
        assertThat(alteredFood.getFat()).isEqualTo(30.0);
        assertThat(alteredFood.getProtein()).isEqualTo(40.0);
        assertThat(alteredFood.getPortions()).hasSize(1);

        PortionDto portion2 = alteredFood.getPortions().stream().filter(p -> p.getDescription().equals("newPortion")).findFirst().get();
        assertThat(portion2.getGrams()).isEqualTo(2.0);
        assertThat(portion2.getId()).isNotNull();
        assertThat(portion2.getMacros()).isNotNull();

        // Alter the portion
        FoodRequest foodWithAlteredPortionRequest = FoodRequest.builder()
                .id(savedFood.getId())
                .carbs(20.0)
                .fat(30.0)
                .protein(40.0)
                .name("newName")
                .portions(Arrays.asList(PortionDto.builder()
                        .description("newPortionName")
                        .grams(3.0)
                        .id(portion2.getId())
                        .build()))
                .build();
        foodService.addFood(foodWithAlteredPortionRequest);

        allFoodEntity = foodService.getAllFood();
        foodDtos = (List<FoodDto>) allFoodEntity.getBody();

        alteredFood = foodDtos.stream().filter(f -> f.getName().equals("newName")).findFirst().get();

        assertThat(alteredFood.getName()).isEqualTo("newName");
        assertThat(alteredFood.getCarbs()).isEqualTo(20.0);
        assertThat(alteredFood.getFat()).isEqualTo(30.0);
        assertThat(alteredFood.getProtein()).isEqualTo(40.0);
        assertThat(alteredFood.getPortions()).hasSize(1);

        PortionDto portionAltered = alteredFood.getPortions().stream().filter(p -> p.getDescription().equals("newPortionName")).findFirst().get();
        assertThat(portionAltered.getGrams()).isEqualTo(3.0);
        assertThat(portionAltered.getId()).isNotNull();
        assertThat(portionAltered.getMacros()).isNotNull();

    }


}
