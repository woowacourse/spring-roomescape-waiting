package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSpec;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.spec.theme
            JOIN FETCH r.spec.time
            WHERE (:memberId IS NULL OR r.member.id = :memberId)
              AND (:themeId IS NULL OR r.spec.theme.id = :themeId)
              AND (:from IS NULL OR r.spec.date >= :from)
              AND (:to IS NULL OR r.spec.date <= :to)
            """)
    List<Reservation> findFiltered(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    boolean existsBySpecDateValueAndSpecTimeId(LocalDate reservationDate, Long id);

    default boolean existsBySpec(ReservationSpec spec) {
        return existsBySpecDateValueAndSpecTimeIdAndSpecThemeId(spec.getDate().getValue(), spec.getTime().getId(),
                spec.getTheme().getId());
    }

    boolean existsBySpecDateValueAndSpecTimeIdAndSpecThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.spec.theme
            JOIN FETCH r.spec.time
            WHERE (r.member.id = :memberId)
            """)
    List<Reservation> findAllByMemberId(Long id);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.spec.theme
            JOIN FETCH r.spec.time
            """)
    List<Reservation> findAllWithEager();
}
