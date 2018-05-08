package csl.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Carmen on 18-3-2018.
 */

@RestController
@RequestMapping("/example")
public class RestService {

    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(path = "", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> getExampleMessage() {

        return new ResponseEntity<>("Hello from the REST service!", HttpStatus.OK);
    }

}
