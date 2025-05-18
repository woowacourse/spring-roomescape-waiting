package roomescape.time.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationAvailability;
import roomescape.time.dto.TimeAvailabilityResponse;
import roomescape.time.repository.ReservationTimeRepository;

@Service
public class ReservationAvailabilityService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationAvailabilityService(final ReservationTimeRepository reservationTimeRepository,
                                          final ThemeRepository themeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<TimeAvailabilityResponse> getAllTimeAvailability(final LocalDate date, final long themeId) {
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("테마가 존재하지 않습니다."));
        final List<ReservationAvailability> reservationAvailabilities = reservationTimeRepository.
                findAllReservationAvailability(date, theme.getId());
        return reservationAvailabilities.stream()
                .map(reservationAvailability -> new TimeAvailabilityResponse(
                        reservationAvailability.getTimeId(),
                        reservationAvailability.getStartAt(),
                        reservationAvailability.isBooked()
                )).toList();
    }
}
