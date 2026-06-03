package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.api.dto.ReservationRequest;
import roomescape.api.dto.ReservationResponses;
import roomescape.api.dto.ReservationUpdateRequest;
import roomescape.application.command.ReservationCommandService;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationTimeQueryService;
import roomescape.application.query.ThemeQueryService;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@Service
@Transactional(readOnly = true)
public class ReservationApplicationService {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;

    public ReservationApplicationService(
            ReservationCommandService reservationCommandService,
            ReservationQueryService reservationQueryService,
            ReservationTimeQueryService reservationTimeQueryService,
            ThemeQueryService themeQueryService
    ) {
        this.reservationCommandService = reservationCommandService;
        this.reservationQueryService = reservationQueryService;
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.themeQueryService = themeQueryService;
    }

    @Transactional
    public Reservation save(ReservationRequest request) {
        ReservationTime reservationTime = reservationTimeQueryService.getById(request.timeId());
        Theme theme = themeQueryService.getById(request.themeId());
        Slot slot = new Slot(
                request.date(),
                reservationTime,
                theme
        );

        return reservationCommandService.save(
                new Member(request.name()),
                slot
        );
    }

    @Transactional
    public Reservation updateMine(Long id, String name, ReservationUpdateRequest request) {
        Reservation existing = reservationQueryService.getById(id);
        ReservationTime newTime = reservationTimeQueryService.getById(request.timeId());
        Slot targetSlot = new Slot(
                request.date(),
                newTime,
                existing.getTheme()
        );

        return reservationCommandService.updateMine(
                existing,
                new Member(name),
                targetSlot
        );
    }

    public ReservationResponses findPage(int page, int size) {
        return reservationQueryService.findPage(page, size);
    }

    public ReservationResponses findMine(String name) {
        return reservationQueryService.findMine(new Member(name));
    }
}
