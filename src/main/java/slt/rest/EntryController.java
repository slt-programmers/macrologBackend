package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.service.EntryService;
import slt.util.LocalDateParser;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/logs")
@AllArgsConstructor
public class EntryController {

    private EntryService entryService;

    @GetMapping(path = "/day/{date}")
    public ResponseEntity<List<EntryDto>> getEntriesForDay(@PathVariable("date") final String date) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var localDate = LocalDateParser.parse(date);
        final var entryDtos = entryService.getEntriesForDay(userInfo.getUserId(), localDate);
        return ResponseEntity.ok(entryDtos);
    }

    @PostMapping(path = "/day/{date}/{meal}")
    public ResponseEntity<List<EntryDto>> postEntries(
            @PathVariable("date") final String date,
            @PathVariable("meal") final String meal,
            @RequestBody final List<EntryDto> entries) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var entryDtos = entryService.postEntries(userInfo.getUserId(), LocalDateParser.parse(date), entries, meal);
        return ResponseEntity.ok(entryDtos);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable("id") final Long entryId) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        entryService.deleteEntry(userInfo.getUserId(), entryId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/macros", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DayMacroDto>> getMacrosForPeriod(@RequestParam("from") final String fromDate, @RequestParam("to") final String toDate) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var parsedFromDate = LocalDateParser.parse(fromDate);
        final var parsedToDate = LocalDateParser.parse(toDate);

        final var dayMacroDtos = entryService.getMacrosForPeriod(userInfo.getUserId(), parsedFromDate, parsedToDate);
        return ResponseEntity.ok(dayMacroDtos);
    }

}
