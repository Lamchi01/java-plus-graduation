package ru.yandex.practicum.compilation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.compilation.model.Compilation;

import java.util.List;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query("""
            SELECT c FROM Compilation c
            WHERE :pinned IS NULL OR c.pinned = :pinned
            """)
    List<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);
}