package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
                SELECT
                   r
                FROM  Reservation r
                JOIN FETCH r.member
                JOIN FETCH r.time
                JOIN FETCH r.theme
                WHERE (:memberId IS NULL OR r.member.id = :memberId)
                AND (:themeId IS NULL OR r.theme.id = :themeId)
                AND (:dateFrom IS NULL OR r.date >= :dateFrom)
                AND (:dateTo IS NULL OR r.date <= :dateTo)
            """)
    List<Reservation> findAllByConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByTime(ReservationTime time);

    boolean existsByTheme(Theme theme);

    Optional<Reservation> findByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<Reservation> findAllByMember(Member member);
}
