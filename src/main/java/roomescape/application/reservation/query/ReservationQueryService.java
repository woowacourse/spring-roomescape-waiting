package roomescape.application.reservation.query;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.query.dto.ReservationSearchCondition;
import roomescape.application.reservation.query.dto.ReservationWithStatusResult;
import roomescape.application.reservation.query.dto.ReservationResult;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public ReservationQueryService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationResult> findAll() {
        List<Reservation> reservations = reservationRepository.findAllWithMemberAndTimeAndTheme();
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationResult> findReservationsBy(ReservationSearchCondition reservationSearchCondition) {
        List<Reservation> reservations = reservationRepository.findByThemeIdAndMemberIdAndDateBetween(
                reservationSearchCondition.themeId(),
                reservationSearchCondition.memberId(),
                reservationSearchCondition.from(),
                reservationSearchCondition.to()
        );
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationWithStatusResult> findReservationsWithStatus(Long memberId) {
        return reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(ReservationWithStatusResult::from)
                .toList();
    }
}
