package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.database.EntryRepository;
import slt.dto.DayMacroDto;
import slt.dto.EntryDto;
import slt.mapper.EntryMapper;
import slt.util.MacroUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class EntryService {

    private EntryRepository entryRepository;

    private final EntryMapper entryMapper = EntryMapper.INSTANCE;

    public List<EntryDto> getEntriesForDay(final Long userId, final LocalDate date) {
        final var allLogEntries = entryRepository.getAllEntries(userId, date);
        return entryMapper.map(allLogEntries);
    }

    public List<DayMacroDto> getMacrosForPeriod(final Long userId, final LocalDate from, final LocalDate to) {
        final var allEntries = entryRepository.getAllEntries(userId, from, to);
        final var entryDtos = entryMapper.map(allEntries);

        final var dateOptionalEntryMap = entryDtos.stream().collect(Collectors.groupingBy(EntryDto::getDay,
                Collectors.reducing((EntryDto d1, EntryDto d2) -> {
                    final var entryDto = new EntryDto();
                    entryDto.setMacrosCalculated(MacroUtils.add(d1.getMacrosCalculated(), d2.getMacrosCalculated()));
                    return entryDto;
                })));

        final var macrosPerDayDto = new ArrayList<DayMacroDto>();
        for (final var dateOptionalEntry : dateOptionalEntryMap.entrySet()) {
            final var dayMacroDto = new DayMacroDto();
            dayMacroDto.setDay(dateOptionalEntry.getKey());
            final var optionalEntryDto = dateOptionalEntry.getValue();
            optionalEntryDto.ifPresent(entryDto -> dayMacroDto.setMacros(entryDto.getMacrosCalculated()));
            macrosPerDayDto.add(dayMacroDto);
        }
        macrosPerDayDto.sort(Comparator.comparing(DayMacroDto::getDay));
        return macrosPerDayDto;
    }

    public List<EntryDto> postEntries(final Long userId, final LocalDate date, final List<EntryDto> entryDtos, final String meal) {
        final var existingEntriesForMeal = entryRepository.getAllEntries(userId, date, meal);

        // Delete old
        final var entryIds = entryDtos.stream().map(EntryDto::getId).toList();
        for (final var existingEntry : existingEntriesForMeal) {
            if (!entryIds.contains(existingEntry.getId())) {
                entryRepository.deleteEntry(userId, existingEntry.getId());
                log.info("Deleting old existingEntry {}", existingEntry.getFood().getId());
            }
        }

        // Add or update
        for (final var entryDto : entryDtos) {
            final var entity = entryMapper.map(entryDto, userId);
            entryRepository.saveEntry(entity);
        }

        final var allEntries = entryRepository.getAllEntries(userId, date);
        return entryMapper.map(allEntries);
    }

    public void deleteEntry(final Long userId, final Long entryId) {
        entryRepository.deleteEntry(userId, entryId);
    }

    public List<EntryDto> getAllEntries(final Long userId) {
        final var allEntries = entryRepository.getAllEntries(userId);
        return entryMapper.map(allEntries);
    }
}
