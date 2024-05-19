package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.support.fixture.ReservationTimeFixture;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("예약 가능한 시간들을 조회한다.")
    @Sql("/reservation.sql")
    void findAvailableReservationTimes() {
        LocalDate date = LocalDate.of(2024, 4, 9);
        List<AvailableReservationTimeDto> availableReservationTimes = reservationTimeRepository
                .findAvailableReservationTimes(date, 1L);

        assertThat(availableReservationTimes).containsExactly(
                new AvailableReservationTimeDto(1L, LocalTime.of(9, 0), false),
                new AvailableReservationTimeDto(2L, LocalTime.of(12, 0), true),
                new AvailableReservationTimeDto(3L, LocalTime.of(17, 0), false),
                new AvailableReservationTimeDto(4L, LocalTime.of(21, 0), true)
        );
    }

    @Test
    @DisplayName("startAt에 해당하는 예약 시간이 존재하는지 확인한다.")
    void existsByStartAt() {
        ReservationTime time = ReservationTimeFixture.startAt("10:00");
        reservationTimeRepository.save(time);

        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).isTrue();
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(11, 0))).isFalse();
    }
}
