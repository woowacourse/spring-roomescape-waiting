package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.ReservationTime;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.util.DatabaseCleaner;
import roomescape.util.ReservationInserter;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReservationTimeRepositoryTest {
    @Autowired
    ReservationTimeRepository sut;
    @Autowired
    ReservationInserter reservationInserter;
    @Autowired
    DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setup() {
        databaseCleaner.initialize();
    }

    @Test
    void create() {
        final var result = sut.save(ReservationTime.from(null, "10:00"));
        assertThat(result).isNotNull();
    }

    @Test
    void isExistByStartAt() {
        sut.save(ReservationTime.from(null, "10:00"));
        final var result = sut.existsByStartAt(LocalTime.parse("10:00"));
        assertThat(result).isTrue();
    }

    @Test
    void findById() {
        final var id = sut.save(ReservationTime.from(null, "10:00"))
                .getId();
        final var result = sut.findById(id);
        assertThat(result).contains(ReservationTime.from(null, "10:00"));
    }

    @Test
    void delete() {
        final var reservationTime = sut.save(ReservationTime.from(null, "10:00"));
        sut.delete(reservationTime);
        final var result = sut.findById(reservationTime.getId());
        assertThat(result).isNotPresent();
    }

    @Test
    void getAll() {
        sut.save(ReservationTime.from(null, "10:00"));
        sut.save(ReservationTime.from(null, "11:00"));

        final var result = sut.findAll();
        assertThat(result).hasSize(2);
    }

    @Test
    void get_available_reservationTime_with_date_and_themeId() {
        final var time = sut.save(ReservationTime.from(null, "12:00"));
        sut.save(ReservationTime.from(null, "13:00"));
        final var reservation = reservationInserter.addNewReservation("2024-10-03", ThemeFixture.getDomain(), MemberFixture.getDomain(), ReservationTimeFixture.getDomain());
        reservationInserter.addExistReservation("2024-10-03", reservation.getTheme(), reservation.getMember(), time);

        final var result =
                sut.getAvailableReservationTimeByThemeIdAndDate(LocalDate.parse("2024-10-03"), reservation.getTheme()
                        .getId());

        result.stream()
                .filter(avail -> avail.getStartAt()
                        .equals("13:00"))
                .forEach(avail -> assertThat(avail.getIsBooked()).isFalse());
    }
}
