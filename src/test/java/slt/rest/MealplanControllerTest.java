package slt.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slt.database.MealplanRepository;
import slt.database.entities.Mealplan;
import slt.dto.MealplanDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MealplanControllerTest {

    private MealplanRepository repository;
    private MealplanController controller;

    @BeforeEach
    void setup() {
        repository = mock(MealplanRepository.class);
        controller = new MealplanController(repository);
        final var userInfo = new UserInfo();
        userInfo.setUserId(1L);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    void getAllMealplans() {
        final var mealplans = List.of(Mealplan.builder().id(1L).title("title").mealtimes(new ArrayList<>()).build());
        when(repository.getAllMealplans(any(Long.class))).thenReturn(mealplans);
        final var result = controller.getAllMealplans();
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(1, result.getBody().size());
        Assertions.assertEquals("title", result.getBody().getFirst().getTitle());
    }

    @Test
    void postMealplan() {
        final var requestDto = MealplanDto.builder().title("my plan").build();
        final var mealplan = Mealplan.builder().id(1L).title("my plan").build();
        when(repository.saveMealplan(any(Mealplan.class))).thenReturn(mealplan);
        final var result = controller.postMealplan(requestDto);
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(1L, result.getBody().getId());
        Assertions.assertEquals("my plan", result.getBody().getTitle());
        Assertions.assertEquals(0, result.getBody().getMealtimes().size());
    }

    @Test
    void putMealplan() {
        final var requestDto = MealplanDto.builder().title("my plan").build();
        final var mealplan = Mealplan.builder().id(1L).title("my plan").build();
        when(repository.saveMealplan(any(Mealplan.class))).thenReturn(mealplan);
        final var result = controller.putMealplan(requestDto);
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(1L, result.getBody().getId());
        Assertions.assertEquals("my plan", result.getBody().getTitle());
        Assertions.assertEquals(0, result.getBody().getMealtimes().size());
    }

    @Test
    void deleteMealplan() {
        final var result = controller.deleteMealplan(1L);
        verify(repository).deleteMealplan(1L, 1L);
        Assertions.assertNull(result.getBody());
    }
}