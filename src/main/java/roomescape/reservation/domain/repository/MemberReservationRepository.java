package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.NotFoundException;
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

    default MemberReservation getById(final Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorType.RESERVATION_TIME_NOT_FOUND,
                        String.format("회원 예약(MemberReservation) 정보가 존재하지 않습니다. [memberReservationId: %d]", id)));
    }

    @Query("""
            SELECT mr
            FROM MemberReservation mr JOIN FETCH mr.reservation r JOIN FETCH r.reservationTime rt JOIN FETCH r.theme t
            WHERE mr.member = :member AND r.date >= CURRENT_DATE AND rt.startAt >= CURRENT_TIME
            ORDER BY mr.id ASC
            """)
    List<MemberReservation> findAfterAndEqualDateReservationByMemberOrderByIdAsc(final Member member);

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

    List<MemberReservation> findByReservation(Reservation reservation);

    Optional<MemberReservation> findByReservationAndMember(Reservation reservation, Member member);

    Long countByReservation(Reservation reservation);

    @Query("""
            SELECT mr
            FROM MemberReservation mr JOIN FETCH mr.reservation r JOIN FETCH r.reservationTime rt JOIN FETCH r.theme t
            WHERE mr.status = :status
            """)
    List<MemberReservation> findByStatus(ReservationStatus status);

    @Query("""
            SELECT mr
            FROM MemberReservation mr JOIN FETCH mr.reservation r JOIN FETCH r.reservationTime rt JOIN FETCH r.theme t
            WHERE mr.status = :status
            GROUP BY mr.id
            HAVING mr.id = MIN(mr.id)
            """)
    List<MemberReservation> findFirstOrderMemberReservationByStatus(ReservationStatus status);
}
