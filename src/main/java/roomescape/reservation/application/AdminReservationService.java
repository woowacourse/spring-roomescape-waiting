package roomescape.reservation.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.request.ReservationSearchServiceRequest;
import roomescape.reservation.application.dto.response.ReservationServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.service.WaitingManagement;

@Service
@RequiredArgsConstructor
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingManagement waitingManagement;

    public List<ReservationServiceResponse> getAll() {
        List<Reservation> reservation = reservationRepository.getAll();
        return reservation.stream()
            .map(ReservationServiceResponse::from)
            .toList();
    }

    public List<ReservationServiceResponse> getSearchedAll(
        ReservationSearchServiceRequest request) {
        List<Reservation> reservations = reservationRepository.getSearchReservations(
            request.themeId(),
            request.memberId(),
            request.dateFrom(),
            request.dateTo()
        );
        return reservations.stream()
            .map(ReservationServiceResponse::from)
            .toList();
    }

    @Transactional
    public void delete(Long id) {
        Reservation reservation = reservationRepository.getById(id);
        reservationRepository.remove(reservation);
        waitingManagement.promoteWaiting(
            reservation.getTheme(),
            reservation.getDate(),
            reservation.getTime()
        ).ifPresent(reservationRepository::save);
    }
}
