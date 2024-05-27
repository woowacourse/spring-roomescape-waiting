package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);

    @Query("""
                      SELECT r, r.time, r.theme, r.member
                        FROM Reservation AS r
                        JOIN FETCH r.theme
                        JOIN FETCH r.member
                        JOIN FETCH r.time
                       WHERE (:memberId = r.member.id OR :memberId IS NULL)
                       AND (:themeId = r.theme.id OR :themeId IS NULL)
                       AND (
                        (:from IS NULL AND :to IS NULL)
                        OR (:to IS NULL AND r.date.date >= :from)
                        OR (:from IS NULL AND r.date.date <= :to) 
                        OR (r.date.date BETWEEN :from AND :to )
                       )
            """)
    List<Reservation> findAllByMemberIdAndThemeIdInPeriod(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    boolean existsByTimeId(Long id);

    @EntityGraph(attributePaths = {"member", "time", "theme"})
    List<Reservation> findAllByDateAndThemeId(ReservationDate date, Long themeId);

    boolean existsByThemeId(Long id);

    @EntityGraph(attributePaths = {"member", "time", "theme"})
    List<Reservation> findAllByMemberId(Long id);

    @EntityGraph(attributePaths = {"member", "time", "theme"})
    List<Reservation> findAll();

    @EntityGraph(attributePaths = {"member", "time", "theme"})
    Optional<Reservation> findByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);
}
