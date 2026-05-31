package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.exception.NotFoundException;
import roomescape.domain.projection.ReservationTimeAvailability;
import roomescape.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeQueryService {

    private final ReservationTimeRepository timeRepository;
    private final ReservationQueryService reservationQueryService;

    public ReservationTimeQueryService(
            ReservationTimeRepository timeRepository,
            ReservationQueryService reservationQueryService
    ) {
        this.timeRepository = timeRepository;
        this.reservationQueryService = reservationQueryService;
    }

    public ReservationTime getById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지않는 예약시간입니다. Id: " + id));
    }

    public List<ReservationTime> findAll() {
        return timeRepository.findAll();
    }

    public List<ReservationTimeAvailability> findWithAvailability(LocalDate date, Long themeId) {
        List<ReservationTime> times = timeRepository.findAll();
        Set<Long> reservedTimeIds = reservationQueryService.findReservedTimeIds(date, themeId);

        return times.stream()
                .map(time -> new ReservationTimeAvailability(time, !reservedTimeIds.contains(time.getId())))
                .toList();
    }
}
