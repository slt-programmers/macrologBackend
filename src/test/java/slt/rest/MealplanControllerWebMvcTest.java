package slt.rest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import slt.database.MealplanRepository;
import slt.database.entities.Mealplan;
import slt.util.JWTBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealplanController.class)
class MealplanControllerWebMvcTest {

    @MockBean
    private MealplanRepository repository;

    private final JWTBuilder jwtBuilder = new JWTBuilder();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllMealplans() throws Exception {
        final var mealplans = List.of(Mealplan.builder().id(1L).title("title").mealtimes(new ArrayList<>()).build());
        Mockito.when(repository.getAllMealplans(any(Integer.class))).thenReturn(mealplans);
        mockMvc.perform(get("/mealplans").header("Authorization", "Bearer " + jwtBuilder.generateJWT("test", 1)))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"id\":1,\"title\":\"title\",\"mealtimes\":[]}]"));
    }

    @Test
    void postMealplan() {
    }

    @Test
    void putMealplan() {
    }

    @Test
    void deleteMealplan() {
    }
}