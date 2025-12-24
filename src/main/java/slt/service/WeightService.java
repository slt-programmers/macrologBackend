package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.database.WeightRepository;
import slt.database.entities.Weight;
import slt.dto.WeightDto;
import slt.mapper.WeightMapper;

import java.sql.Date;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class WeightService {

    private WeightRepository weightRepository;

    private final WeightMapper weightMapper = WeightMapper.INSTANCE;

    public WeightDto saveWeight(final Long userId, final WeightDto weightDto) {
        final var existingEntities = weightRepository.getWeightEntryForDay(userId, Date.valueOf(weightDto.getDay()));
        final var entity = weightMapper.map(weightDto, userId);
        mapForExistingDate(entity, existingEntities);
        final var savedWeight = weightRepository.saveWeight(entity);
        return weightMapper.map(savedWeight);
    }

    private void mapForExistingDate(final Weight weight, final List<Weight> existingEntities) {
        final var date = weight.getDay();
        final var optionalEntity = existingEntities.stream().filter(w -> w.getDay().toString().equals(date.toString()))
                .findFirst();
        if (optionalEntity.isPresent()) {
            final var entity = optionalEntity.get();
            if (!entity.getId().equals(weight.getId())) {
                weight.setId(entity.getId());
            }
        }
    }

}
