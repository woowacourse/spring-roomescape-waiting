package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.api.dto.ReservationWaitingRequest;
import roomescape.application.service.ReservationQueryService;
import roomescape.application.service.ReservationWaitingCommandService;
import roomescape.application.service.ReservationWaitingQueryService;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.exception.ConflictException;
import roomescape.domain.projection.ReservationWaitingWithOrder;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingApplicationService {

    private static final String WAITING_REQUIRES_RESERVED_SLOT = "예약된 슬롯에만 대기를 신청할 수 있습니다.";

    private final ReservationWaitingCommandService reservationWaitingCommandService;
    private final ReservationWaitingQueryService reservationWaitingQueryService;
    private final ReservationQueryService reservationQueryService;

    public ReservationWaitingApplicationService(
            ReservationWaitingCommandService reservationWaitingCommandService,
            ReservationWaitingQueryService reservationWaitingQueryService,
            ReservationQueryService reservationQueryService
    ) {
        this.reservationWaitingCommandService = reservationWaitingCommandService;
        this.reservationWaitingQueryService = reservationWaitingQueryService;
        this.reservationQueryService = reservationQueryService;
    }

    @Transactional
    public ReservationWaitingWithOrder save(ReservationWaitingRequest request) {
        Reservation reservation = getWaitingTargetReservation(request);
        ReservationWaiting reservationWaiting = reservationWaitingCommandService.save(request.name(), reservation);

        return reservationWaitingQueryService.getWithOrderById(reservationWaiting.getId());
    }

    public List<ReservationWaitingWithOrder> findMine(String name) {
        return reservationWaitingQueryService.findMine(name);
    }

    @Transactional
    public void deleteMine(Long id, String name) {
        ReservationWaiting reservationWaiting = reservationWaitingQueryService.getById(id);

        reservationWaitingCommandService.deleteMine(reservationWaiting, name);
    }

    private Reservation getWaitingTargetReservation(ReservationWaitingRequest request) {
        return reservationQueryService.findBySlot(
                request.date(),
                request.timeId(),
                request.themeId()
        ).orElseThrow(() -> new ConflictException(WAITING_REQUIRES_RESERVED_SLOT));
    }
}
