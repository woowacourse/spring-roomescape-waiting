package roomescape.repository;

import static roomescape.domain.exception.DomainErrorCode.WAITLIST_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Waitlist;
import roomescape.domain.exception.RoomEscapeException;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    List<Waitlist> findByName(String name);

    @Query("""
            select count(w)
            from Waitlist w
            where w.date = :date
              and w.time.id = :timeId
              and w.theme.id = :themeId
              and (
                  w.createdAt < :createdAt
                  or (
                      w.createdAt = :createdAt
                      and w.id < :id
                  )
              )
            """)
    int countBefore(
            @Param("date") LocalDate date,
            @Param("timeId") Long timeId,
            @Param("themeId") Long themeId,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("id") Long id
    );

    boolean existsByTime_Id(Long timeId);

    boolean existsByTheme_Id(Long themeId);

    boolean existsByNameAndDateAndTime_IdAndTheme_Id(
            String name,
            LocalDate date,
            Long timeId,
            Long themeId
    );

    @EntityGraph(attributePaths = {"time", "theme"})
    Optional<Waitlist> findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(
            LocalDate date,
            Long timeId,
            Long themeId);

    default Waitlist getById(Long id, String message) {
        return findById(id).orElseThrow(() -> new RoomEscapeException(WAITLIST_NOT_FOUND, message));
    }
}
