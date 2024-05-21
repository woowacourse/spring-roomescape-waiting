package roomescape.application.reservation;

import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.request.ReservationFilterRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.application.reservation.dto.response.ReservationStatusResponse;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationStatusRepository;
import roomescape.infrastructure.reservation.ReservationSpec;

@Service
@Transactional(readOnly = true)
public class ReservationLookupService {
    private final ReservationRepository reservationRepository;
    private final ReservationStatusRepository reservationStatusRepository;

    public ReservationLookupService(ReservationRepository reservationRepository,
                                    ReservationStatusRepository reservationStatusRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationStatusRepository = reservationStatusRepository;
    }

    public List<ReservationResponse> findByFilter(ReservationFilterRequest request) {
        Specification<Reservation> specification = ReservationSpec.where()
                .equalsMemberId(request.memberId())
                .equalsThemeId(request.themeId())
                .greaterThanOrEqualsStartDate(request.startDate())
                .lessThanOrEqualsEndDate(request.endDate())
                .build();

        return reservationRepository.findAll(specification)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllBookedReservations() {
        return reservationStatusRepository.findAllBookedReservations()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationStatusResponse> getReservationStatusesByMemberId(long memberId) {
        return reservationStatusRepository.findActiveReservationStatusesByMemberId(memberId)
                .stream()
                .map(ReservationStatus::getReservation)
                .map(reservation -> {
                    long waitingCount = reservationStatusRepository.getWaitingCount(reservation);
                    return ReservationStatusResponse.of(reservation, waitingCount);
                }).toList();
    }

    public List<ReservationResponse> findAllWaitingReservations() {
        return reservationStatusRepository.findAllWaitingReservations()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
