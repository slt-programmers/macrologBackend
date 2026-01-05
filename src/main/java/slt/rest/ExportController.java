package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import slt.database.*;
import slt.database.entities.*;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.service.*;

@Slf4j
@RestController
@RequestMapping("/export")
@AllArgsConstructor
public class ExportController {

    private ExportService exportService;

    @GetMapping
    public ResponseEntity<Export> getAll() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var export = exportService.exportAllForUser(userInfo.getUserId());
        return ResponseEntity.ok(export);
    }

}
