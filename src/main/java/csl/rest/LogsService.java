package csl.rest;

import csl.database.FoodRepository;
import csl.database.model.Food;
import csl.dto.FoodMacros;
import csl.dto.LogEntry;
import csl.dto.Macro;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/logs")
@Api(value="logs", description="Operations pertaining to logentries in the macro logger applications")
public class LogsService {

    // :o omg even een static in memory lijstje :p
    private static List<LogEntry> inMemory = new ArrayList<>();

    @ApiOperation(value = "Retrieve all stored logentries")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllLogEntries() {
        return ResponseEntity.ok(inMemory);
    }

    @ApiOperation(value = "Store new logentry")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeLogEntry(@RequestBody LogEntry logEntry) {
        inMemory.add(logEntry);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
