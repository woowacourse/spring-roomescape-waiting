package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.query.ReservationQueryService;
import roomescape.domain.Member;
import roomescape.presentation.dto.ReservationResponses;

@Service
public class ReservationApplicationService {

    private final ReservationQueryService reservationQueryService;

    public ReservationApplicationService(
            ReservationQueryService reservationQueryService
    ) {
        this.reservationQueryService = reservationQueryService;
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
