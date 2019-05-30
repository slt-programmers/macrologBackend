package csl.rest;

import csl.database.WeightRepository;
import csl.database.model.Weight;
import csl.dto.WeightDto;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/weight")
@Api(value = "weight", description = "Operations pertaining to weightRepository tracking in the macro logger application")
public class WeightTrackerService {

    private WeightRepository weightRepository = new WeightRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(WeightTrackerService.class);

    @ApiOperation(value = "Retrieve all tracked weights")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllWeightEntries() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Weight> allLogEntries = weightRepository.getAllWeightEntries(userInfo.getUserId());

        return ResponseEntity.ok(mapToDtos(userInfo, allLogEntries));
    }

    @ApiOperation(value = "Store weight entry")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeWeightEntries(@RequestBody WeightDto logEntry) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<WeightDto> newEntries = new ArrayList<>();
//        for (WeightDto logEntry : logEntries) {
            Weight entry = new Weight();
            entry.setDay(new Date(logEntry.getDay().getTime()));
            entry.setId(logEntry.getId());
            entry.setWeight(logEntry.getWeight());
            if (logEntry.getId() == null) {
                weightRepository.insertWeight(userInfo.getUserId(), entry);
                List<Weight> addedEntryMatches = weightRepository.getWeightEntryForDay(userInfo.getUserId(), entry.getDay());
                if (addedEntryMatches.size() > 1) {
                    Weight newestEntry = addedEntryMatches.stream().max(Comparator.comparing(Weight::getId)).orElseThrow(() -> new IllegalArgumentException("Weight not found"));
                    addedEntryMatches = new ArrayList<>();
                    addedEntryMatches.add(newestEntry);
                }
                if (addedEntryMatches.size() != 1) {
                    LOGGER.error("SAVE OF WEIGHT NOT SUCCEEDED " + userInfo.getUserId() + " - " + entry.getWeight() + " - " + entry.getDay());
                }
                newEntries.add(mapToDto(userInfo, addedEntryMatches.get(0)));
            } else {
                weightRepository.updateWeight(userInfo.getUserId(), entry);
                newEntries.add(mapToDto(userInfo,entry));
            }
//        }

        return ResponseEntity.ok(newEntries);
    }

    @ApiOperation(value = "Delete weight entry")
    @RequestMapping(value = "/{id}",
            method = DELETE,
            headers = {"Content-Type=application/json"})
    public ResponseEntity deleteWeightEntry(@PathVariable("id") Long weightEntryId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        weightRepository.deleteWeight(userInfo.getUserId(), weightEntryId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    private List<WeightDto> mapToDtos(UserInfo userInfo, List<Weight> allWeightEntries) {
        List<WeightDto> allDtos = new ArrayList<>();
        for (Weight weightEntry : allWeightEntries) {

            WeightDto dto = mapToDto(userInfo, weightEntry);
            allDtos.add(dto);
        }

        return allDtos;
    }

    private WeightDto mapToDto(UserInfo userInfo, Weight logEntry) {
        WeightDto dto = new WeightDto();
        dto.setDay(logEntry.getDay());
        dto.setId(logEntry.getId());
        dto.setWeight(logEntry.getWeight());

        return dto;
    }
}
