package roomescape.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@DomainService
public class PopularThemeFinder {
    private static final int START_DAY_TO_SUBTRACT = 8;
    private static final int END_DATE_TO_SUBTRACT = 1;
    private static final int COUNT_OF_LIMIT = 10;

    private final ReservationQueryRepository reservationQueryRepository;
    private final Clock clock;

    public PopularThemeFinder(ReservationQueryRepository reservationQueryRepository, Clock clock) {
        this.reservationQueryRepository = reservationQueryRepository;
        this.clock = clock;
    }

    public List<Theme> findThemes() {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = today.minusDays(START_DAY_TO_SUBTRACT);
        LocalDate endDate = today.minusDays(END_DATE_TO_SUBTRACT);
        return reservationQueryRepository.findPopularThemesDateBetween(startDate, endDate).stream()
                .limit(COUNT_OF_LIMIT).toList();
    }
}
