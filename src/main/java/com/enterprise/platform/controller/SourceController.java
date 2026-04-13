package com.enterprise.platform.controller;

import com.enterprise.platform.model.DataSource;
import com.enterprise.platform.repository.DataSourceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
public class SourceController {

    private final DataSourceRepository sourceRepo;

    public SourceController(DataSourceRepository sourceRepo) {
        this.sourceRepo = sourceRepo;
    }

    @GetMapping
    public ResponseEntity<List<DataSource>> listSources() {
        return ResponseEntity.ok(sourceRepo.findAll());
    }

    @PostMapping
    public ResponseEntity<DataSource> createSource(@RequestBody DataSource source) {
        return ResponseEntity.ok(sourceRepo.save(source));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<DataSource> toggleSource(@PathVariable Long id) {
        DataSource source = sourceRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + id));
        source.setEnabled(!source.isEnabled());
        return ResponseEntity.ok(sourceRepo.save(source));
    }
}
