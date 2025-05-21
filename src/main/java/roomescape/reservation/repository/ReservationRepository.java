package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.WaitingReservationWithRank;
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

    List<Reservation> findByMemberAndStatus(final Member member, ReservationStatus status);

    @Query("""
                select new roomescape.reservation.dto.WaitingReservationWithRank(r, 
                  (select COUNT(rw)
                   from Reservation rw
                   where rw.theme = r.theme AND rw.date = r.date AND rw.time = r.time AND rw.createdAt <= r.createdAt AND rw.status = 'WAITING')
                )
                FROM Reservation r
                WHERE r.member = :member
                  AND r.status = 'WAITING'
            """)
    List<WaitingReservationWithRank> findWaitingReservationByMemberWithRank(@Param("member") Member member);
}
