package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long> {

    List<MemberReservation> findByMember(final Member member);

    @Query("""
            SELECT mr
            FROM MemberReservation mr JOIN FETCH mr.reservation r JOIN FETCH r.reservationTime rt JOIN FETCH r.theme t
            WHERE mr.member = :member AND rt = :time AND t = :theme AND r.date = :date
            """)
    Optional<MemberReservation> findByMemberAndReservationTimeAndDateAndTheme(Member member, ReservationTime time, LocalDate date, Theme theme);

    @Query("""
            SELECT mr
            FROM MemberReservation mr JOIN FETCH mr.reservation r JOIN FETCH r.reservationTime rt JOIN FETCH r.theme t
            WHERE r.date = :date AND rt = :time AND t = :theme
            ORDER BY mr.id ASC
            """)
    List<MemberReservation> findByReservationTimeAndDateAndThemeOrderByIdAsc(ReservationTime time, LocalDate date, Theme theme);

    Optional<MemberReservation> findByReservation(Reservation reservation);

    @Query("""
            SELECT mr
            FROM MemberReservation mr JOIN FETCH mr.reservation r JOIN FETCH r.reservationTime rt JOIN FETCH r.theme t
            WHERE mr.status = :status
            """)
    List<MemberReservation> findByStatus(ReservationStatus status);
}
