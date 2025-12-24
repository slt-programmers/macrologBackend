package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.database.entities.Food;
import slt.database.entities.LogEntry;
import slt.database.entities.Portion;
import slt.dto.EntryDto;
import slt.dto.PortionDto;

import java.sql.Date;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ImportService {

    public Food getFoodFromListByName(final String foodName, final List<Food> foodList) {
        final var foodMatches = foodList.stream()
                .filter(food -> food.getName().equals(foodName))
                .toList();
        if (foodMatches.size() > 1) {
            log.error("Multiple Food with name {} found", foodName);
            log.error("Selecting first from list");
        }
        return foodMatches.getFirst();
    }

    // TODO move to mapper
    public LogEntry mapLogEntryDtoToLogEntry(final EntryDto entryDto) {
        final var logEntry = new LogEntry();
        logEntry.setId(null);
        final var newDate = new Date(entryDto.getDay().getTime());
        logEntry.setDay(newDate);
        logEntry.setFoodId(entryDto.getFood().getId());
        logEntry.setMeal(entryDto.getMeal().toString());
        logEntry.setMultiplier(entryDto.getMultiplier());
        if (entryDto.getPortion() != null) {
            logEntry.setPortionId(entryDto.getPortion().getId());
        }
        return logEntry;
    }

    // TODO move to mapper
    public Portion mapPortionDtoToPortion(final PortionDto portionDto) {
        final var portion = new Portion();
        portion.setId(null);
        portion.setGrams(portionDto.getGrams());
        portion.setDescription(portionDto.getDescription());
        return portion;
    }

}
