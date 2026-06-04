package roomescape.time.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.NotFoundException;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.service.dto.AvailableTimesResult;
import roomescape.time.service.dto.ReservationTimeResult;

@Service
@Transactional(readOnly = true)
public class ReservationTimeQueryService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeService themeService;

    public ReservationTimeQueryService(ReservationTimeRepository reservationTimeRepository,
                                       ThemeService themeService) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeService = themeService;
    }

    public List<ReservationTimeResult> findAll() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResult::from)
                .toList();
    }

    public ReservationTimeResult findById(long id) {
        ReservationTime reservationTime = reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(TimeErrorCode.TIME_NOT_FOUND.getMessage()));
        return ReservationTimeResult.from(reservationTime);
    }

    public AvailableTimesResult queryAvailableTimes(long themeId, LocalDate date) {
        themeService.findById(themeId);
        return new AvailableTimesResult(reservationTimeRepository.queryAvailableTimes(themeId, date));
    }
}
