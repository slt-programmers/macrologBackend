package csl;

import csl.database.model.Food;
import csl.rest.FoodService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
        FoodService fc = new FoodService();
//        fc.insertFood("Banana");
        List<Food> allFood = fc.getAllFood();
        System.out.println(allFood.toString());

    }
}