package slt.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.WeightRepository;
import slt.database.entities.Weight;
import slt.mapper.MyModelMapper;
import slt.dto.WeightDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/weight")
public class WeightService {

    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    MyModelMapper myModelMapper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WeightDto>> getAllWeightEntries() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Weight> allWeightEntries = weightRepository.getAllWeightEntries(userInfo.getUserId());
        List<WeightDto> collectedDtos = allWeightEntries
                .stream()
                .map(weight -> myModelMapper.getConfiguredMapper().map(weight, WeightDto.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(collectedDtos);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> storeWeightEntry(@RequestBody WeightDto weightEntry) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();

        Weight entry = myModelMapper.getConfiguredMapper().map(weightEntry, Weight.class);
        List<Weight> storedWeight = weightRepository.getWeightEntryForDay(userInfo.getUserId(), entry.getDay());

        boolean weightRegisteredOnSameDay = (storedWeight != null && !storedWeight.isEmpty());

        if (weightRegisteredOnSameDay && weightEntry.getId() != null && storedWeight.get(0).getId().equals(weightEntry.getId().intValue())) {
            // Simpele update
            Weight weightUpdate = storedWeight.get(0);
            weightUpdate.setRemark(entry.getRemark());
            weightUpdate.setValue(entry.getValue());
            weightRepository.updateWeight(userInfo.getUserId(), weightUpdate);
        } else if (weightRegisteredOnSameDay && (weightEntry.getId() == null || !storedWeight.get(0).getId().equals(weightEntry.getId().intValue()))) {
            // Update de reeds bestaande weight met de nieuwe entries.
            Weight weightToBeUpdated = storedWeight.get(0);
            weightToBeUpdated.setRemark(entry.getRemark());
            weightToBeUpdated.setValue(entry.getValue());
            weightToBeUpdated.setDay(entry.getDay());
            weightRepository.updateWeight(userInfo.getUserId(), weightToBeUpdated);
        } else if (!weightRegisteredOnSameDay && entry.getId() == null) {
            // Opslaan van een nieuwe weight
            entry.setUserId(userInfo.getUserId());
            weightRepository.insertWeight(userInfo.getUserId(), entry);
        } else if (!weightRegisteredOnSameDay && entry.getId() != null) {
            // update van entry
            weightRepository.updateWeight(userInfo.getUserId(), entry);
        } else {
            throw new UnsupportedOperationException("Niet afgevangen update van weight");
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteWeightEntry(@PathVariable("id") Long weightEntryId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        weightRepository.deleteWeightByIdAndUserId(weightEntryId, userInfo.getUserId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
