package roomescape.time.service;

import org.springframework.stereotype.Service;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservableTime;
import roomescape.time.dto.ReservableTimeResponse;
import roomescape.time.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservableTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservableTimeService(final ReservationTimeRepository reservationTimeRepository,
                                 final ThemeRepository themeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservableTimeResponse> findReservableTimes(final LocalDate date, final long themeId) {
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("테마가 존재하지 않습니다."));
        final List<ReservableTime> reservableTimes = reservationTimeRepository.
                findAllReservableTime(date, theme.getId());
        return reservableTimes.stream()
                .map(reservableTime -> new ReservableTimeResponse(
                        reservableTime.getTimeId(),
                        reservableTime.getStartAt(),
                        reservableTime.isBooked()
                )).toList();
    }
}
