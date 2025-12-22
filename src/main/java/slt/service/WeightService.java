package slt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.database.entities.Weight;

import java.util.List;

@Slf4j
@Service
public class WeightService {

    public void mapForExistingDate(final Weight weight, final List<Weight> existingEntities) {
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
