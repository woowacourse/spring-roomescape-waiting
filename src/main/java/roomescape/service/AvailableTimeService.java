package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.AvailableTimeResponse;
import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class AvailableTimeService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public AvailableTimeService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<AvailableTimeResponse> findByThemeAndDate(LocalDate date, long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomescapeException(ExceptionType.NOT_FOUND_THEME));

        HashSet<ReservationTime> alreadyUsedTimes = new HashSet<>(
                reservationRepository.findAllByDateAndTheme(date, theme)
                        .stream()
                        .map(Reservation::getReservationTime)
                        .toList());

        return reservationTimeRepository.findAll()
                .stream()
                .map(reservationTime -> AvailableTimeResponse.of(
                        reservationTime, alreadyUsedTimes.contains(reservationTime)))
                .toList();
    }
}
