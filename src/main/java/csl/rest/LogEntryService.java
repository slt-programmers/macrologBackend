package csl.rest;

import csl.database.FoodRepository;
import csl.database.LogEntryRepository;
import csl.database.PortionRepository;
import csl.database.model.Food;
import csl.database.model.LogEntry;
import csl.database.model.Portion;
import csl.dto.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sun.rmi.runtime.Log;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/logs")
@Api(value = "logs", description = "Operations pertaining to logentries in the macro logger applications")
public class LogEntryService {

    private FoodRepository foodRepository = new FoodRepository();
    private PortionRepository portionRepository = new PortionRepository();
    private LogEntryRepository logEntryRepository = new LogEntryRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryService.class);


    @ApiOperation(value = "Retrieve all stored logentries")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllLogEntries() {
        List<csl.database.model.LogEntry> allLogEntries = logEntryRepository.getAllLogEntries();

        return ResponseEntity.ok(mapToDtos(allLogEntries));
    }

    @ApiOperation(value = "Retrieve all stored logentries")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/day/{date}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getLogOfDay(@PathVariable("date") String dateLog) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date parsedDate;
        try {
            parsedDate = sdf.parse(dateLog);
        } catch (ParseException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        List<csl.database.model.LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(parsedDate);

        List<LogEntryDto> allDtos = new ArrayList<>();
        for (csl.database.model.LogEntry logEntry : allLogEntries) {

            LogEntryDto dto = new LogEntryDto();
            Food food = foodRepository.getFoodById(logEntry.getFoodId());
            dto.setId(logEntry.getId());
            FoodDto foodDto = FoodService.mapFoodToFoodDto(food);
            dto.setFood(foodDto);

            Portion portion = null;
            if (logEntry.getPortionId() != null && logEntry.getPortionId() != 0) {
                portion = portionRepository.getPortion(logEntry.getPortionId());
                PortionDto portionDto = new PortionDto();
                portionDto.setId(portion.getId());
                portionDto.setGrams(portion.getGrams());
                portionDto.setDescription(portion.getDescription());
                portionDto.setUnitMultiplier(portion.getUnitMultiplier());
                Macro calculatedMacros = FoodService.calculateMacro(food, portion);
                portionDto.setMacros(calculatedMacros);
                dto.setPortion(portionDto);
            }
            Double multiplier = logEntry.getMultiplier();
            dto.setMultiplier(multiplier);
            dto.setDay(logEntry.getDay());
            dto.setMeal(logEntry.getMeal());

            Macro macrosCalculated = new Macro();
            if (portion != null) {
                macrosCalculated = dto.getPortion().getMacros().clone();
                macrosCalculated.multiply(multiplier);

            } else {
                macrosCalculated.setCarbs(multiplier * food.getCarbs());
                macrosCalculated.setFat(multiplier * food.getFat());
                macrosCalculated.setProtein(multiplier * food.getProtein());
            }
            dto.setMacrosCalculated(macrosCalculated);

            allDtos.add(dto);
        }
        return ResponseEntity.ok(allDtos);


    }

    @ApiOperation(value = "Store logentries")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeLogEntries(@RequestBody List<StoreLogEntryRequest> logEntries) {

        for (StoreLogEntryRequest logEntry : logEntries) {
            csl.database.model.LogEntry entry = new csl.database.model.LogEntry();
            entry.setPortionId(logEntry.getPortionId());
            entry.setFoodId(logEntry.getFoodId());
            entry.setMultiplier(logEntry.getMultiplier());
            entry.setDay(new Date(logEntry.getDay().getTime()));
            entry.setMeal(logEntry.getMeal());
            entry.setId(logEntry.getId());
            if (logEntry.getId() == null) {
                logEntryRepository.insertLogEntry(entry);
            } else {
                logEntryRepository.updateLogEntry(entry);
            }
        }


        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @ApiOperation(value = "Delete logentry")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/{id}",
            method = DELETE,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeLogEntry(@PathVariable("id") Long logEntryId) {

        logEntryRepository.deleteLogEntry(logEntryId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/macros",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getMacrosFromPeriod(@RequestParam("from") String fromDate, @RequestParam("to") String toDate) {

        Calendar now = GregorianCalendar.getInstance();
        java.util.Date endTime = now.getTime();
        Calendar begin = GregorianCalendar.getInstance();
        begin.add(GregorianCalendar.MONTH,-1);
        java.util.Date beginTime = begin.getTime();

        List<LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(beginTime, endTime);
        List<LogEntryDto> logEntryDtos = mapToDtos(allLogEntries);
        LOGGER.debug("Aantal dtos:" + logEntryDtos);

        Map<java.util.Date, List<LogEntryDto>> mappedPerDay = logEntryDtos.stream().collect(Collectors.groupingBy(LogEntryDto::getDay));
        Map<java.util.Date, List<LogEntryDto>> mappedPerDay2 = logEntryDtos.stream().collect(Collectors.groupingBy(logentrydto -> logentrydto.getDay()));
        LOGGER.debug("Aantal dagen mapped:" + mappedPerDay.size());




        BiConsumer<LogEntryDto,LogEntryDto> reducer = (object1,object2) -> {
            object1.getMacrosCalculated().setProtein(object1.getMacrosCalculated().getProtein() + object2.getMacrosCalculated().getProtein());
            object1.getMacrosCalculated().setFat(object1.getMacrosCalculated().getFat() + object2.getMacrosCalculated().getFat());
            object1.getMacrosCalculated().setCarbs(object1.getMacrosCalculated().getCarbs() + object2.getMacrosCalculated().getCarbs());
        };
        BinaryOperator<LogEntryDto> operator = (object1, object2) -> {
            object1.getMacrosCalculated().setProtein(object1.getMacrosCalculated().getProtein() + object2.getMacrosCalculated().getProtein());
            object1.getMacrosCalculated().setFat(object1.getMacrosCalculated().getFat() + object2.getMacrosCalculated().getFat());
            object1.getMacrosCalculated().setCarbs(object1.getMacrosCalculated().getCarbs() + object2.getMacrosCalculated().getCarbs());
            return object1;
        };

        Map<java.util.Date, List<LogEntryDto>> collect = logEntryDtos.stream().collect(Collectors.groupingBy(o -> o.getDay()));



        //        mappedPerDay.forEach((day, listday) -> listday.stream().collect(() -> new LogEntryDto(),reducer,reducer));
//        mappedPerDay.forEach((day, listday) -> System.out.println(day + " -" + listday.size()));
//        mappedPerDay.forEach((day,logentriesperday) --> reducer);
      // mappedPerDay.forEach((day,lisday) -> lisday.stream().map(sumElements
//        mappedPerDay.forEach((day,listday)-> listday.stream().reduce(new LogEntryDto(),operator));



//
//        for (List<LogEntryDto> entryDtos : mappedPerDay.values()) {
//            for (LogEntryDto entryDto : entryDtos) {
//                System.out.println(entryDto.getDay() +"\t"+entryDto.getMacrosCalculated().getProtein() + "\t" + entryDto.getMacrosCalculated().getFat() + "\t" + entryDto.getMacrosCalculated().getCarbs());
//            }
//
//        }
//

        ArrayList<DayMacro> retObject = new ArrayList<>();
        GregorianCalendar gc = new GregorianCalendar();
        gc.add(GregorianCalendar.MONTH, -1);
        for (int i = 0; i <= 1; i++) {
            Macro macro = new Macro();
            macro.setFat(31 * Math.random());
            macro.setCarbs(31 * Math.random());
            macro.setProtein(31 * Math.random());
            DayMacro dm = new DayMacro();
            dm.setDay(gc.getTime());
            gc.add(GregorianCalendar.DAY_OF_YEAR, 1);
            dm.setMacro(macro);
            retObject.add(dm);
        }
        return ResponseEntity.ok(retObject);
    }


//    public static List<DayMacro> sumElements(java.util.Date d, List<LogEntryDto> elements) {
//        return new ArrayList<>();
//    }
    public static List<Macro> sumElements(List<LogEntryDto> elements) {
        return new ArrayList<>();
    }
    public static List<Macro> sumMacros(List<Macro> elements) {
        return new ArrayList<>();
    }

    private List<LogEntryDto> mapToDtos(List<LogEntry> allLogEntries){
        List<LogEntryDto> allDtos = new ArrayList<>();
        for (csl.database.model.LogEntry logEntry : allLogEntries) {

            LogEntryDto dto = new LogEntryDto();
            Food food = foodRepository.getFoodById(logEntry.getFoodId());
            dto.setId(logEntry.getId());
            FoodDto foodDto = FoodService.mapFoodToFoodDto(food);
            dto.setFood(foodDto);

            Portion portion = null;
            if (logEntry.getPortionId() != null && logEntry.getPortionId() != 0) {
                portion = portionRepository.getPortion(logEntry.getPortionId());
                PortionDto portionDto = new PortionDto();
                portionDto.setId(portion.getId());
                portionDto.setGrams(portion.getGrams());
                portionDto.setDescription(portion.getDescription());
                portionDto.setUnitMultiplier(portion.getUnitMultiplier());
                Macro calculatedMacros = FoodService.calculateMacro(food, portion);
                portionDto.setMacros(calculatedMacros);
                dto.setPortion(portionDto);
            }
            Double multiplier = logEntry.getMultiplier();
            dto.setMultiplier(multiplier);
            dto.setDay(logEntry.getDay());
            dto.setMeal(logEntry.getMeal());

            Macro macrosCalculated = new Macro();
            if (portion != null) {
                macrosCalculated = dto.getPortion().getMacros().clone();
                macrosCalculated.multiply(multiplier);

            } else {
                macrosCalculated.setCarbs(multiplier * food.getCarbs());
                macrosCalculated.setFat(multiplier * food.getFat());
                macrosCalculated.setProtein(multiplier * food.getProtein());
            }
            dto.setMacrosCalculated(macrosCalculated);

            allDtos.add(dto);
        }
        return  allDtos;
    }
}
