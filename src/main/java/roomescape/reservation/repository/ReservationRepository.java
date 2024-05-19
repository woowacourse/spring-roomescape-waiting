package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate fromDate,
                                                            LocalDate toDate);

    boolean existsByDateAndReservationTimeStartAt(LocalDate date, LocalTime startAt);

    @Query("""
            select r.reservationTime.id
            from Reservation r
            where r.date = :date and r.theme.id = :themeId
            """)
    List<Long> findIdByReservationsDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);

    @EntityGraph(attributePaths = {"theme"})
    List<Reservation> findByDateBetween(LocalDate fromDate, LocalDate toDate);
}
