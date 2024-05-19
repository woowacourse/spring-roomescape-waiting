package roomescape.domain.theme;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import roomescape.domain.DomainService;
import roomescape.domain.reservation.ReservationRepository;

@DomainService
public class PopularThemeFinder {
    private static final int START_DAY_TO_SUBTRACT = 8;
    private static final int END_DATE_TO_SUBTRACT = 1;
    private static final int COUNT_OF_LIMIT = 10;

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public PopularThemeFinder(ReservationRepository reservationRepository, Clock clock) {
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    public List<Theme> findThemes() {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = today.minusDays(START_DAY_TO_SUBTRACT);
        LocalDate endDate = today.minusDays(END_DATE_TO_SUBTRACT);
        return reservationRepository.findPopularThemesDateBetween(startDate, endDate).stream()
                .limit(COUNT_OF_LIMIT).toList();
    }
}
