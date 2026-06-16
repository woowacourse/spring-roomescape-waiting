package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.reservation.fixture.ReservationFixture.reservation;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationDateRepository reservationDateRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private List<ReservationTime> saveAll(List<ReservationTime> reservationTimes) {
        List<ReservationTime> savedTimes = new ArrayList<>();
        for (ReservationTime reservationTime : reservationTimes) {
            savedTimes.add(saveTime(reservationTime));
        }
        return savedTimes;
    }

    private ReservationTime saveTime(ReservationTime reservationTime) {
        return reservationTimeRepository.save(reservationTime);
    }

    private ReservationDate saveDate(ReservationDate reservationDate) {
        return reservationDateRepository.save(reservationDate);
    }

    private Theme saveTheme(Theme theme) {
        return themeRepository.save(theme);
    }

    private void saveReservation(ReservationDate reservationDate, ReservationTime reservationTime,
        Theme theme) {
        reservationRepository.save(reservation("송송", reservationDate, reservationTime, theme));
    }

    @Nested
    @DisplayName("findAvailableTimes 메서드는")
    class FindAvailableTimesTest {


        @Test
        @DisplayName("예약 가능 시간을 조회한다")
        void 성공() {
            // given
            ReservationTime reservedTime15 = saveTime(ReservationTimeFixture.activeTime15());
            ReservationTime reservedTime16 = saveTime(ReservationTimeFixture.activeTime16());
            ReservationTime nonReservedTime = saveTime(ReservationTimeFixture.activeTime17());

            ReservationDate date = saveDate(ReservationDateFixture.oneWeekLater());
            Theme theme = saveTheme(ThemeFixture.activeTheme());

            saveReservation(date, reservedTime15, theme);
            saveReservation(date, reservedTime16, theme);

            // when
            List<ReservationTime> availableTimes =
                reservationTimeRepository.findAllByIsActiveTrue();

            // then
            assertThat(availableTimes)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactlyInAnyOrder(
                    reservedTime15,
                    reservedTime16,
                    nonReservedTime
                );
        }
    }
}
