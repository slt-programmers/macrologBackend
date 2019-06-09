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
@Api(value = "weight")
public class WeightService {

    private WeightRepository weightRepository = new WeightRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(WeightService.class);

    @ApiOperation(value = "Retrieve all tracked weights")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllWeightEntries() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Weight> allWeightEntries = weightRepository.getAllWeightEntries(userInfo.getUserId());

        return ResponseEntity.ok(mapToDtos(allWeightEntries));
    }

    @ApiOperation(value = "Store weight entry")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeWeightEntries(@RequestBody WeightDto weightEntry) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Weight entry = mapWeightToDomain(weightEntry);
        List<Weight> storedWeight = weightRepository.getWeightEntryForDay(userInfo.getUserId(), entry.getDay());
        if (weightEntry.getId() == null && (storedWeight == null || storedWeight.size() == 0)) {
            weightRepository.insertWeight(userInfo.getUserId(), entry);
        } else {
            weightRepository.updateWeight(userInfo.getUserId(), entry);
        }

        return ResponseEntity.ok().build();
    }

    private Weight mapWeightToDomain(@RequestBody WeightDto weightEntry) {
        Weight entry = new Weight();
        entry.setDay(Date.valueOf(weightEntry.getDay()));
        entry.setId(weightEntry.getId());
        entry.setWeight(weightEntry.getWeight());
        return entry;
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


    private List<WeightDto> mapToDtos(List<Weight> allWeightEntries) {
        List<WeightDto> allDtos = new ArrayList<>();
        for (Weight weightEntry : allWeightEntries) {
            WeightDto dto = mapToDto(weightEntry);
            allDtos.add(dto);
        }
        return allDtos;
    }

    private WeightDto mapToDto(Weight weightEntry) {
        WeightDto dto = new WeightDto();
        dto.setDay(weightEntry.getDay().toLocalDate());
        dto.setId(weightEntry.getId());
        dto.setWeight(weightEntry.getWeight());

        return dto;
    }
}
