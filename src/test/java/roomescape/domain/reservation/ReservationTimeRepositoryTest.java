package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.reservation.dto.AvailableReservationTimeDto;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("시작 시간으로 예약 시간이 존재하는지 확인한다.")
    void existsByStartAt() {
        ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        boolean exists = reservationTimeRepository.existsByStartAt(savedReservationTime.getStartAt());

        assertThat(exists).isTrue();
    }

    @Test
    @Sql("/available-reservation-times.sql")
    @DisplayName("이용 가능한 시간들을 조회한다.")
    void findAvailableReservationTimes() {
        LocalDate date = LocalDate.of(2024, 4, 9);
        Long themeId = 1L;

        List<AvailableReservationTimeDto> response = reservationTimeRepository
                .findAvailableReservationTimes(date, themeId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response).hasSize(4);

            softly.assertThat(response.get(0).id()).isEqualTo(1L);
            softly.assertThat(response.get(0).startAt()).isEqualTo("09:00");
            softly.assertThat(response.get(0).alreadyBooked()).isFalse();

            softly.assertThat(response.get(1).id()).isEqualTo(2L);
            softly.assertThat(response.get(1).startAt()).isEqualTo("12:00");
            softly.assertThat(response.get(1).alreadyBooked()).isTrue();

            softly.assertThat(response.get(2).id()).isEqualTo(3L);
            softly.assertThat(response.get(2).startAt()).isEqualTo("17:00");
            softly.assertThat(response.get(2).alreadyBooked()).isFalse();

            softly.assertThat(response.get(3).id()).isEqualTo(4L);
            softly.assertThat(response.get(3).startAt()).isEqualTo("21:00");
            softly.assertThat(response.get(3).alreadyBooked()).isTrue();
        });
    }

    @Test
    @DisplayName("아이디로 예약 시간을 조회한다.")
    void getById() {
        ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        ReservationTime reservationTime = reservationTimeRepository.getById(savedReservationTime.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservationTime.getId()).isNotNull();
            softly.assertThat(reservationTime.getStartAt()).isEqualTo("10:00");
        });
    }

    @Test
    @DisplayName("아이디로 예약 시간을 조회하고, 없을 경우 예외를 발생시킨다.")
    void getByIdWhenNotExist() {
        assertThatThrownBy(() -> reservationTimeRepository.getById(-1L))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessage(String.format("해당 id의 예약 시간이 존재하지 않습니다. (id: %d)", -1L));
    }
}
