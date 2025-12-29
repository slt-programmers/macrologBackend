package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import slt.database.*;
import slt.dto.*;
import slt.mapper.*;
import slt.security.ThreadLocalHolder;
import slt.service.ImportService;

@Slf4j
@RestController
@RequestMapping("/import")
@AllArgsConstructor
public class ImportController {

    private ImportService importService;

    @PostMapping
    public ResponseEntity<Void> setAll(@RequestBody final Export export) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        importService.importAllForUser(userInfo.getUserId(), export);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
