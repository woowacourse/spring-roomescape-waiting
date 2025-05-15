package roomescape.reservation.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.application.dto.request.ReservationSearchServiceRequest;
import roomescape.reservation.application.dto.response.ReservationServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.dto.ReservationWithMember;

@Service
@RequiredArgsConstructor
public class AdminReservationService {

    private final ReservationRepository reservationRepository;

    public List<ReservationServiceResponse> getAll() {
        List<ReservationWithMember> reservationWithMembers = reservationRepository.getAllWithMember();
        return reservationWithMembers.stream()
                .map(ReservationServiceResponse::from)
                .toList();
    }

    public List<ReservationServiceResponse> getSearchedAll(ReservationSearchServiceRequest request) {
        List<ReservationWithMember> reservationWithMembers = reservationRepository.getSearchReservationsWithMember(
                request.themeId(),
                request.memberId(),
                request.dateFrom(),
                request.dateTo()
        );
        return reservationWithMembers.stream()
                .map(ReservationServiceResponse::from)
                .toList();
    }

    public void delete(Long id) {
        Reservation reservation = reservationRepository.getById(id);
        reservationRepository.remove(reservation);
    }
}
