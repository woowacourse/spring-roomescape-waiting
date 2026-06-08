package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.api.dto.ReservationRequest;
import roomescape.api.dto.ReservationResponses;
import roomescape.application.command.ReservationCommandService;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationTimeQueryService;
import roomescape.application.query.ThemeQueryService;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.ConflictException;

@Service
public class ReservationApplicationService {

    private static final String ALREADY_EXISTS_RESERVATION = "해당 날짜와 시간, 테마에 이미 예약이 존재합니다.";

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
        reservationCommandService.findBySlotForUpdate(slot)
                .ifPresent(reservation -> {
                    throw new ConflictException(ALREADY_EXISTS_RESERVATION);
                });

        return reservationCommandService.save(
                new Member(request.name()),
                slot
        );
    }

    @Transactional(readOnly = true)
    public ReservationResponses findPage(int page, int size) {
        return reservationQueryService.findPage(page, size);
    }

    @Transactional(readOnly = true)
    public ReservationResponses findMine(String name) {
        return reservationQueryService.findMine(new Member(name));
    }
}
