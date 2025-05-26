package roomescape.admin.service.facade;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.admin.dto.AdminReservationResponse;
import roomescape.admin.dto.ReservationSearchRequest;
import roomescape.admin.dto.ReservationWaitingResponse;
import roomescape.admin.service.reservation.AdminReservationService;
import roomescape.admin.service.waiting.AdminWaitingService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminReservationService reservationService;
    private final AdminWaitingService waitingService;

    @Transactional
    public AdminReservationResponse saveByAdmin(final AdminReservationRequest adminReservationRequest) {
        final LocalDate date = adminReservationRequest.date();
        final Long themeId = adminReservationRequest.themeId();
        final Long timeId = adminReservationRequest.timeId();
        final Long memberId = adminReservationRequest.memberId();

        final Reservation savedReservation = reservationService.saveByAdmin(date, themeId, timeId, memberId);

        return AdminReservationResponse.from(savedReservation);
    }

    @Transactional
    public void deleteWaitingById(final Long id) {
        waitingService.deleteWaitingById(id);
    }

    @Transactional(readOnly = true)
    public List<AdminReservationResponse> findByInFromTo(final ReservationSearchRequest searchRequest) {
        final Long themeId = searchRequest.themeId();
        final Long memberId = searchRequest.memberId();
        final LocalDate from = searchRequest.dateFrom();
        final LocalDate to = searchRequest.dateTo();

        final List<Reservation> reservations = reservationService.findByInFromTo(themeId, memberId, from, to);

        return reservations.stream()
                .map(AdminReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationWaitingResponse> findAllWaitingReservations() {
        final List<Waiting> waitingReservations = waitingService.findAllWaitingReservations();

        return waitingReservations.stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }
}
