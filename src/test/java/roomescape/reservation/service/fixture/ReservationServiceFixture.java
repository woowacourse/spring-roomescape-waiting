package roomescape.reservation.service.fixture;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.policy.ReservationPolicy;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.test_config.MutableTimeManager;
import roomescape.test_config.TestTimeManagerConfig;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.theme.repository.ThemeRepository;

@JdbcTest
@Import({
        TestTimeManagerConfig.class,
        ReservationService.class,
        JdbcReservationRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class,
        ReservationPolicy.class
})
public abstract class ReservationServiceFixture {

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected ReservationTimeRepository reservationTimeRepository;

    @Autowired
    protected ThemeRepository themeRepository;

    @Autowired
    protected MutableTimeManager timeManager;

    protected void insertConfirmedReservation(
            LocalDate date,
            ReservationTime time,
            Theme theme,
            String guestName
    ) {
        insertReservation(guestName, date, time, theme, Status.CONFIRMED);
    }

    protected Reservation insertWaitingReservation(
            LocalDate date,
            ReservationTime time,
            String guestName
    ) {
        Theme theme = insertTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://example.com/theme.png"
        );

        return insertReservation(
                guestName,
                date,
                time,
                theme,
                Status.WAITING
        );
    }

    protected ReservationTime insertReservationTime(LocalTime startAt) {
        return reservationTimeRepository.save(
                ReservationTime.create(startAt)
        );
    }

    protected Theme insertTheme(
            String name,
            String description,
            String thumbnail
    ) {
        return themeRepository.save(
                Theme.create(name, description, thumbnail)
        );
    }

    protected Reservation insertReservation(
            String guestName,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            Status status
    ) {
        return reservationRepository.save(
                Reservation.create(
                        guestName,
                        ReservationSlot.of(date, time, theme),
                        status
                )
        );
    }
}
