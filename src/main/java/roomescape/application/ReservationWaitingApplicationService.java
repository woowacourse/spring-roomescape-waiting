package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.command.ReservationWaitingCommandService;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationTimeQueryService;
import roomescape.application.query.ReservationWaitingQueryService;
import roomescape.application.query.ThemeQueryService;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.ConflictException;
import roomescape.domain.projection.ReservationWaitingWithOrder;
import roomescape.presentation.dto.ReservationWaitingRequest;

@Service
public class ReservationWaitingApplicationService {

    private static final String WAITING_REQUIRES_RESERVED_SLOT = "예약된 슬롯에만 대기를 신청할 수 있습니다.";

    private final ReservationWaitingCommandService reservationWaitingCommandService;
    private final ReservationWaitingQueryService reservationWaitingQueryService;
    private final ReservationQueryService reservationQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;

    public ReservationWaitingApplicationService(
            ReservationWaitingCommandService reservationWaitingCommandService,
            ReservationWaitingQueryService reservationWaitingQueryService,
            ReservationQueryService reservationQueryService,
            ReservationTimeQueryService reservationTimeQueryService,
            ThemeQueryService themeQueryService
    ) {
        this.reservationWaitingCommandService = reservationWaitingCommandService;
        this.reservationWaitingQueryService = reservationWaitingQueryService;
        this.reservationQueryService = reservationQueryService;
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.themeQueryService = themeQueryService;
    }

    @Transactional
    public ReservationWaitingWithOrder save(ReservationWaitingRequest request) {
        ReservationTime reservationTime = reservationTimeQueryService.getById(request.timeId());
        Theme theme = themeQueryService.getById(request.themeId());
        Slot slot = new Slot(
                request.date(),
                reservationTime,
                theme
        );

        Reservation reservation = reservationQueryService.findBySlot(slot)
                .orElseThrow(() -> new ConflictException(WAITING_REQUIRES_RESERVED_SLOT));

        ReservationWaiting reservationWaiting = reservationWaitingCommandService.save(
                new Member(request.name()),
                reservation
        );

        return reservationWaitingQueryService.getWithOrderById(reservationWaiting.getId());
    }

    @Transactional(readOnly = true)
    public List<ReservationWaitingWithOrder> findMine(String name) {
        return reservationWaitingQueryService.findMine(new Member(name));
    }

    @Transactional
    public void deleteMine(Long id, String name) {
        ReservationWaiting reservationWaiting = reservationWaitingQueryService.getById(id);

        reservationWaitingCommandService.deleteMine(
                reservationWaiting,
                new Member(name)
        );
    }
}
