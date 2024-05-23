package roomescape.domain;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import roomescape.domain.repository.ReservationDetailRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;

@RequiredArgsConstructor
public class ReservationDetailFactory {
    private final ReservationDetailRepository reservationDetailRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationDetail createReservationDetail(LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = reservationTimeRepository.getById(timeId);
        Theme theme = themeRepository.getById(themeId);
        return reservationDetailRepository.getByDateAndTimeAndTheme(date, time, theme);
    }
}
