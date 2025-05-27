package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
              AND (:from IS NULL OR r.spec.date.value >= :from)
              AND (:to IS NULL OR r.spec.date.value <= :to)
            """)
    List<Reservation> findFiltered(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

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
    List<Reservation> findAllByMemberId(Long memberId);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.spec.theme
            JOIN FETCH r.spec.time
            """)
    List<Reservation> findAllWithEager();

    @Query("""
            SELECT r.spec.time.id
            FROM Reservation r
            WHERE r.spec.date.value = :date AND r.spec.theme.id = :themeId
            """)
    List<Long> findTimeIdsByDateAndThemeId(LocalDate date, Long themeId);

    boolean existsBySpecTimeId(Long timeId);

    boolean existsBySpecThemeId(Long themeId);

    default Optional<Reservation> findBySpec(ReservationSpec spec) {
        return findBySpecDateValueAndSpecTimeIdAndSpecThemeId(spec.getDate().getValue(), spec.getTime().getId(),
                spec.getTheme().getId());
    }

    Optional<Reservation> findBySpecDateValueAndSpecTimeIdAndSpecThemeId(LocalDate value, Long timeId, Long themeId);
}
