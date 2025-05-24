package roomescape.reservation.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.request.ReservationSearchServiceRequest;
import roomescape.reservation.application.dto.response.ReservationServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.vo.ReservationStatus;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.service.ReservationOperation;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationOperation reservationOperation;

    public List<ReservationServiceResponse> getAllByStatuses(List<ReservationStatus> statuses) {
        List<Reservation> reservations = reservationRepository.getAllByStatuses(statuses);
        return reservations.stream()
                .map(ReservationServiceResponse::from)
                .toList();
    }

    public List<ReservationServiceResponse> getSearchedAll(ReservationSearchServiceRequest request) {
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
    public void cancel(Long id) {
        Reservation reservation = reservationRepository.getById(id);
        reservationOperation.cancel(reservation);
    }
}
