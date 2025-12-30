package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.database.MealplanRepository;
import slt.dto.MealplanDto;
import slt.mapper.MealplanMapper;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MealplanService {

    private MealplanRepository mealplanRepository;

    private final MealplanMapper mealplanMapper = MealplanMapper.INSTANCE;

    public List<MealplanDto> getAllMealplans(final Long userId) {
        final var mealplans = mealplanRepository.getAllMealplans(userId);
        return mealplanMapper.map(mealplans);
    }

    public MealplanDto saveMealplan(final Long userId, final MealplanDto mealplanDto) {
        final var mealplan = mealplanMapper.map(mealplanDto, userId);
        final var savedPlan = mealplanRepository.saveMealplan(mealplan);
        return mealplanMapper.map(savedPlan);
    }

    public void deleteMealplan(final Long userId, final Long mealplanId) {
        mealplanRepository.deleteMealplan(userId, mealplanId);
    }
}
