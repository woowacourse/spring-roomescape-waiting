package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.api.dto.ReservationTimeRequest;
import roomescape.application.service.ReservationQueryService;
import roomescape.application.service.ReservationTimeCommandService;
import roomescape.application.service.ReservationTimeQueryService;
import roomescape.domain.ReservationTime;
import roomescape.domain.projection.ReservationTimeAvailability;

@Service
@Transactional(readOnly = true)
public class ReservationTimeApplicationService {

    private final ReservationTimeCommandService reservationTimeCommandService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ReservationQueryService reservationQueryService;

    public ReservationTimeApplicationService(
            ReservationTimeCommandService reservationTimeCommandService,
            ReservationTimeQueryService reservationTimeQueryService,
            ReservationQueryService reservationQueryService
    ) {
        this.reservationTimeCommandService = reservationTimeCommandService;
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.reservationQueryService = reservationQueryService;
    }

    @Transactional
    public ReservationTime save(ReservationTimeRequest request) {
        ReservationTime time = new ReservationTime(request.startAt());

        return reservationTimeCommandService.save(time);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeQueryService.findAll();
    }

    public List<ReservationTimeAvailability> findWithAvailability(LocalDate date, Long themeId) {
        List<ReservationTime> times = reservationTimeQueryService.findAll();
        Set<Long> reservedTimeIds = reservationQueryService.findReservedTimeIds(date, themeId);

        return times.stream()
                .map(time -> new ReservationTimeAvailability(time, !reservedTimeIds.contains(time.getId())))
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        reservationTimeCommandService.delete(id);
    }
}
