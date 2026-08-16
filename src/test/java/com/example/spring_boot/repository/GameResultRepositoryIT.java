package com.example.spring_boot.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.example.spring_boot.entity.GameResultEntity;
import com.example.spring_boot.support.AbstractDatabaseIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Runs against a real MySQL 8 container, not H2, so the mapping is proven
 * against the actual production database engine. {@code Replace.NONE} keeps
 * the Testcontainers-supplied datasource in place; without it, {@code
 * @DataJpaTest} swaps in an embedded database by default.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GameResultRepositoryIT extends AbstractDatabaseIT {

    @Autowired
    private GameResultRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private static GameResultEntity newResult(String generationsJson) {
        GameResultEntity entity = new GameResultEntity();
        entity.setBoardName("BLINKER");
        entity.setSteps(5);
        entity.setGenerationsJson(generationsJson);
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    @Test
    @DisplayName("saving a result generates a non-null UUID id")
    void savingGeneratesUuidId() {
        GameResultEntity saved = repository.saveAndFlush(newResult("[]"));

        assertThat(saved.getId()).isNotNull().isInstanceOf(UUID.class);
    }

    @Test
    @DisplayName("a large generationsJson round-trips unchanged, proving @Lob maps to a LONGTEXT column")
    void largeGenerationsJsonRoundTrips() {
        // A plain VARCHAR(255), which is what a bare String column defaults
        // to, would truncate this silently at the database and fail this
        // assertion. 100 KB is comfortably past that boundary.
        String large = "[" + "1".repeat(100_000) + "]";

        GameResultEntity saved = repository.saveAndFlush(newResult(large));
        entityManager.clear(); // force the next read to hit the database, not the persistence context

        GameResultEntity reloaded = repository.findAll().stream()
                .filter(e -> e.getId().equals(saved.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(reloaded.getGenerationsJson()).isEqualTo(large);
    }

    @Test
    @DisplayName("createdAt round-trips without losing precision")
    void createdAtRoundTrips() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        GameResultEntity entity = newResult("[]");
        entity.setCreatedAt(now);

        GameResultEntity saved = repository.saveAndFlush(entity);
        entityManager.clear();

        GameResultEntity reloaded = repository.findAll().stream()
                .filter(e -> e.getId().equals(saved.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(reloaded.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("findAll returns every saved row")
    void findAllReturnsSavedRows() {
        repository.saveAndFlush(newResult("[]"));
        repository.saveAndFlush(newResult("[]"));

        assertThat(repository.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }
}
