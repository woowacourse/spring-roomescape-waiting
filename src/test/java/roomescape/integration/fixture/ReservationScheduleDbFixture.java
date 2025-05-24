package roomescape.integration.fixture;

import org.springframework.stereotype.Component;
import roomescape.domain.reservation.schdule.ReservationDate;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.repository.ReservationScheduleRepository;

@Component
public class ReservationScheduleDbFixture {

    private ReservationScheduleRepository reservationScheduleRepository;

    public ReservationScheduleDbFixture(final ReservationScheduleRepository reservationScheduleRepository) {
        this.reservationScheduleRepository = reservationScheduleRepository;
    }

    public ReservationSchedule 예약_일정_25_4_22(
            final ReservationTime time,
            final Theme theme
    ) {
        ReservationDate date = ReservationDateFixture.예약날짜_25_4_22;
        return createSchedule(date, time, theme);
    }

    public ReservationSchedule 예약_일정_오늘(
            final ReservationTime time,
            final Theme theme
    ) {
        ReservationDate date = ReservationDateFixture.예약날짜_오늘;
        return createSchedule(date, time, theme);
    }

    public ReservationSchedule createSchedule(
            final ReservationDate reservationDate,
            final ReservationTime reservationTime,
            final Theme theme
    ) {
        return reservationScheduleRepository.save(new ReservationSchedule(
                null,
                reservationDate,
                reservationTime,
                theme
        ));
    }
}
