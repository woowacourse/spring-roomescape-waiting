package roomescape.infrastructure.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.ThemeFixture.TEST_THEME;
import static roomescape.fixture.TimeFixture.ELEVEN_AM;
import static roomescape.fixture.TimeFixture.ONE_PM;
import static roomescape.fixture.TimeFixture.TEN_AM;
import static roomescape.fixture.TimeFixture.TWELVE_PM;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.TimeSlot;
import roomescape.fixture.TimeFixture;

@DataJpaTest
class ReservationTimeJpaRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Test
    @DisplayName("특정 시간이 저장소에 존재하면 true를 반환한다.")
    void shouldReturnTrueWhenReservationTimeAlreadyExist() {
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);
        boolean exists = timeRepository.existsByStartAt(time.getStartAt());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 시간이 저장소에 존재하지 않으면 false를 반환한다.")
    void shouldReturnFalseWhenReservationTimeAlreadyExist() {
        LocalTime startAt = LocalTime.of(10, 0);
        boolean exists = timeRepository.existsByStartAt(startAt);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("날짜와 테마 id가 주어지면, 예약 가능한 시간을 반환한다.")
    void shouldReturnAvailableTimes() {
        Theme theme = TEST_THEME.create();
        entityManager.persist(theme);
        Stream.of(TEN_AM, ELEVEN_AM, TWELVE_PM, ONE_PM)
                .map(TimeFixture::create)
                .forEach(entityManager::persist);
        LocalDate date = LocalDate.of(2024, 12, 25);

        List<TimeSlot> times = timeRepository.getReservationTimeAvailabilities(date, theme.getId())
                .stream()
                .filter(TimeSlot::isAvailable)
                .toList();
        assertThat(times).hasSize(4);
    }
}
