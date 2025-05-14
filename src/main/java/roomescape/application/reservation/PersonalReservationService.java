package roomescape.application.reservation;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.application.reservation.dto.ReservationWithStatusResult;
import roomescape.domain.reservation.ReservationRepository;

@Service
public class PersonalReservationService {

    private final ReservationRepository reservationRepository;

    public PersonalReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationWithStatusResult> findReservationsWithStatus(Long memberId) {
        return reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(ReservationWithStatusResult::from)
                .toList();
    }
}
