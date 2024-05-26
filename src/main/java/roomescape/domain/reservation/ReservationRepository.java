package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.member.Member;
import roomescape.domain.reservationdetail.ReservationDetail;
import roomescape.exception.reservation.NotFoundReservationException;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    default Reservation getById(Long id) {
        return findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findAllByStatus(Status status);

    List<Reservation> findByPeriodAndThemeAndMember(LocalDate start, LocalDate end, Long memberId, Long themeId);

    List<ReservationWithRank> findWithRank(Long memberId);

    boolean existsByDetailAndMemberAndStatusNot(ReservationDetail detail, Member member, Status status);

    boolean existsByDetailAndStatus(ReservationDetail reservationDetail, Status status);

    Optional<Reservation> findNextWaitingReservation(ReservationDetail detail);
}
