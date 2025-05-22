package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r FROM Reservation r
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
              AND (:memberId IS NULL OR r.member.id = :memberId)
              AND (:localDateFrom IS NULL OR r.date >= :localDateFrom)
              AND (:localDateTo IS NULL OR r.date <= :localDateTo)
            """)
    List<Reservation> findByCriteria(
            @Param("themeId") Long themeId,
            @Param("memberId") Long memberId,
            @Param("localDateFrom") LocalDate localDateFrom,
            @Param("localDateTo") LocalDate localDateTo
    );

    boolean existsByDateAndTimeAndTheme(final LocalDate date, final ReservationTime time, final Theme theme);

    boolean existsByThemeId(final Long themeId);

    boolean existsByTimeId(final Long timeId);

    List<Reservation> findByMember(final Member member);
}
