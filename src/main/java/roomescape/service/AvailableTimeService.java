package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.AvailableTimeResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;
import static roomescape.service.mapper.AvailableTimeResponseMapper.toResponse;

@Service
@Transactional
public class AvailableTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public AvailableTimeService(ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<AvailableTimeResponse> findByThemeAndDate(LocalDate date, long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));

        HashSet<ReservationTime> alreadyUsedTimes = new HashSet<>(
                reservationTimeRepository.findUsedTimeByDateAndTheme(date, theme));

        return reservationTimeRepository.findAll()
                .stream()
                .map(reservationTime -> toResponse(reservationTime, alreadyUsedTimes.contains(reservationTime)))
                .toList();
    }
}
