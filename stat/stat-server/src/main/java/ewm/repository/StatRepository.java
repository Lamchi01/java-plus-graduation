package ewm.repository;

import ewm.ViewStats;
import ewm.model.EndpointHit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Integer> {
    @Query("""
                SELECT new ewm.ViewStats(e.app, e.uri, COUNT(e.id))
                FROM EndpointHit e
                WHERE e.timestamp BETWEEN ?1 AND ?2 AND (?3 IS NULL OR e.uri IN ?3)
                GROUP BY e.app, e.uri
                ORDER BY COUNT(e.id) DESC
            """)
    List<ViewStats> findAllByTimestampBetweenAndUriIn(LocalDateTime start,
                                                      LocalDateTime end,
                                                      List<String> uris);

    @Query("""
            SELECT new ewm.ViewStats(e.app, e.uri, COUNT(DISTINCT e.ip))
            FROM EndpointHit e
            WHERE e.timestamp BETWEEN ?1 AND ?2 AND (?3 IS NULL OR e.uri IN ?3)
            GROUP BY e.app, e.uri
            ORDER BY COUNT(DISTINCT e.ip) DESC
            """)
    List<ViewStats> findAllUniqueIpAndTimestampBetweenAndUriIn(@Param("start") LocalDateTime start,
                                                               @Param("end") LocalDateTime end,
                                                               @Param("uris") List<String> uris);
}