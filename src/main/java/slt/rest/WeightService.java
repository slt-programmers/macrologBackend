package slt.rest;

import slt.database.WeightRepository;
import slt.database.model.Weight;
import slt.dto.WeightDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/weight")
@Api(value = "weight")
public class WeightService {

    @Autowired
    private WeightRepository weightRepository;

    @ApiOperation(value = "Retrieve all tracked weights")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllWeightEntries() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Weight> allWeightEntries = weightRepository.getAllWeightEntries(userInfo.getUserId());
        List<WeightDto> collectedDtos = allWeightEntries.stream().map(this::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(collectedDtos);
    }

    @ApiOperation(value = "Store weight entry")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeWeightEntry(@RequestBody WeightDto weightEntry) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Weight entry = mapWeightDtoToDomain(weightEntry);
        List<Weight> storedWeight = weightRepository.getWeightEntryForDay(userInfo.getUserId(), entry.getDay());

        boolean weightDayAlreadyRegistered = (storedWeight != null && storedWeight.size() > 0);

        if (weightEntry.getId() == null && !weightDayAlreadyRegistered) {
            weightRepository.insertWeight(userInfo.getUserId(), entry);
        } else {
            if (weightDayAlreadyRegistered) {
                entry.setId(storedWeight.get(0).getId());
            }
            weightRepository.updateWeight(userInfo.getUserId(), entry);
        }

        return ResponseEntity.ok().build();
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

    private WeightDto mapToDto(Weight weightEntry) {
        WeightDto dto = new WeightDto();
        dto.setDay(weightEntry.getDay().toLocalDate());
        dto.setId(weightEntry.getId());
        dto.setWeight(weightEntry.getWeight());
        dto.setRemark(weightEntry.getRemark());
        return dto;
    }

    private Weight mapWeightDtoToDomain(@RequestBody WeightDto weightEntry) {
        Weight entry = new Weight();
        entry.setDay(Date.valueOf(weightEntry.getDay()));
        entry.setId(weightEntry.getId());
        entry.setWeight(weightEntry.getWeight());
        entry.setRemark(weightEntry.getRemark());
        return entry;
    }
}
