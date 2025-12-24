package slt.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.WeightRepository;
import slt.dto.WeightDto;
import slt.mapper.WeightMapper;
import slt.security.ThreadLocalHolder;
import slt.service.WeightService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/weight")
public class WeightController {

    private WeightRepository weightRepository;
    private WeightService weightService;

    private final WeightMapper weightMapper = WeightMapper.INSTANCE;

    @GetMapping
    public ResponseEntity<List<WeightDto>> getAllWeight() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var weightEntities = weightRepository.getAllWeightEntries(userInfo.getUserId());
        final var weightDtos = weightMapper.map(weightEntities);
        return ResponseEntity.ok(weightDtos);
    }

    @PostMapping
    public ResponseEntity<WeightDto> postWeight(@RequestBody final WeightDto weightDto) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var savedWeight = weightService.saveWeight(userInfo.getUserId(), weightDto);
        return ResponseEntity.ok(savedWeight);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteWeightEntry(@PathVariable("id") final Long weightId) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        weightRepository.deleteWeightByIdAndUserId(weightId, userInfo.getUserId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
