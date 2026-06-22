package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.command.ReservationTimeCommandService;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationTimeQueryService;
import roomescape.application.query.ThemeQueryService;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.projection.ReservationTimeAvailability;
import roomescape.presentation.dto.ReservationTimeRequest;

@Service
public class ReservationTimeApplicationService {

    private final ReservationTimeCommandService reservationTimeCommandService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ReservationQueryService reservationQueryService;
    private final ThemeQueryService themeQueryService;

    public ReservationTimeApplicationService(
            ReservationTimeCommandService reservationTimeCommandService,
            ReservationTimeQueryService reservationTimeQueryService,
            ReservationQueryService reservationQueryService,
            ThemeQueryService themeQueryService
    ) {
        this.reservationTimeCommandService = reservationTimeCommandService;
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.reservationQueryService = reservationQueryService;
        this.themeQueryService = themeQueryService;
    }

    @Transactional
    public ReservationTime save(ReservationTimeRequest request) {
        ReservationTime time = new ReservationTime(request.startAt());

        return reservationTimeCommandService.save(time);
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findAll() {
        return reservationTimeQueryService.findAll();
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeAvailability> findWithAvailability(LocalDate date, Long themeId) {
        List<ReservationTime> times = reservationTimeQueryService.findAll();
        Theme theme = themeQueryService.getById(themeId);
        Set<Long> reservedTimeIds = reservationQueryService.findReservedTimeIds(date, theme);

        return times.stream()
                .map(time -> new ReservationTimeAvailability(time, !reservedTimeIds.contains(time.getId())))
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        reservationTimeCommandService.delete(id);
    }
}
