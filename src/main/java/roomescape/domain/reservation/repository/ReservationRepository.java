package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(@Param("timeId") Long reservationTimeId);

    boolean existsByDateAndTimeIdAndThemeId(@Param("date") LocalDate reservationDate,
                                            @Param("timeId") Long timeId,
                                            @Param("themeId") Long themeId);

    @Query("""   
            SELECT r
            FROM Reservation r
            WHERE r.id = :id
            """)
    @EntityGraph(attributePaths = {"time", "theme"})
    Optional<Reservation> findByIdWithTimeAndTheme(@Param("id") Long id);

    boolean existsByThemeId(@Param("themeId") Long themeId);

    List<Reservation> findByThemeIdAndDate(@Param("themeId") Long themeId, @Param("date") LocalDate reservationDate);

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
              AND (:memberId IS NULL OR r.member.id = :memberId)
              AND (:from IS NULL OR r.date >= :from)
              AND (:to IS NULL OR r.date <= :to)
            """)
    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(@Param("themeId") Long themeId,
                                                             @Param("memberId") Long memberId,
                                                             @Param("from") LocalDate from,
                                                             @Param("to") LocalDate to);

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("""
            SELECT r FROM Reservation r
                JOIN FETCH r.member m
                JOIN FETCH r.time t
                JOIN FETCH r.theme th
            """)
    List<Reservation> findAllWithMemberAndTimeAndTheme();

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(@Param("date") LocalDate date,
                                                       @Param("timeId") Long timeId,
                                                       @Param("themeId") Long themeId,
                                                       @Param("memberId") Long memberId);
}
