package com.enterprise.platform.repository;

import com.enterprise.platform.model.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DataSourceRepository extends JpaRepository<DataSource, Long> {

    Optional<DataSource> findBySourceName(String sourceName);

    List<DataSource> findByEnabledTrue();

    List<DataSource> findByIngestionMode(DataSource.IngestionMode mode);
}
