package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.repository.dto.ReservationRankResponse;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByMemberAndThemeAndDateAndTime(Member member, Theme theme, LocalDate date,
                                                 ReservationTime reservationTime);

    @Query("""
            SELECT new roomescape.repository.dto.ReservationRankResponse
            (r.id, r.theme.name, r.date, r.time.startAt, 
            (SELECT count(r2) AS waiting_rank
            FROM Reservation r2
            WHERE r.id >= r2.id AND r.time = r2.time AND r.date = r2.date AND r.theme = r2.theme)
            )
            FROM Reservation r
            WHERE r.member = :member
            """)
    List<ReservationRankResponse> findReservationRankByMember(Member member);
}
