package slt.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slt.database.MealplanRepository;
import slt.database.entities.Mealplan;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MealplanControllerTest {

    private MealplanRepository repository;
    private MealplanController controller;

    @BeforeEach
    void setup() {
        repository = mock(MealplanRepository.class);
        controller = new MealplanController(repository);
        final var userInfo = new UserInfo();
        userInfo.setUserId(1);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    void getAllMealplans() {
        final var mealplans = List.of(Mealplan.builder().id(1L).title("title").mealtimes(new ArrayList<>()).build());
        when(repository.getAllMealplans(any(Integer.class))).thenReturn(mealplans);
        final var result = controller.getAllMealplans();
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(1, result.getBody().size());
        Assertions.assertEquals("title", result.getBody().getFirst().getTitle());
    }
}