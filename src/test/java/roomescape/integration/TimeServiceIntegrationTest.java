package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationTimeDao;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.reservation.service.dto.AvailableTimeInfo;
import roomescape.reservation.service.dto.ReservationTimeCreateCommand;
import roomescape.reservation.service.dto.ReservationTimeInfo;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql({"/schema.sql", "/test-data.sql"})
public class TimeServiceIntegrationTest {

    @Autowired
    ReservationTimeDao reservationTimeDao;

    @Autowired
    ReservationTimeService reservationTimeService;

    @DisplayName("이미 존재하는 예약시간을 추가하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenCreateDuplicatedTime() {
        // given
        ReservationTimeCreateCommand request = new ReservationTimeCreateCommand(LocalTime.of(10, 0));
        // when
        // then
        assertThatThrownBy(() -> reservationTimeService.createReservationTime(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 시간입니다.");
    }

    @DisplayName("예약 시간을 추가할 수 있다")
    @Test
    void createReservationTime() {
        // given
        LocalTime time = LocalTime.of(17, 0);
        ReservationTimeCreateCommand request = new ReservationTimeCreateCommand(time);
        // when
        ReservationTimeInfo result = reservationTimeService.createReservationTime(request);
        // then
        ReservationTime savedTime = reservationTimeDao.findById(result.id()).get();
        assertAll(
                () -> assertThat(result.id()).isEqualTo(4L),
                () -> assertThat(result.startAt()).isEqualTo(time),
                () -> assertThat(savedTime.getId()).isEqualTo(4L),
                () -> assertThat(savedTime.getStartAt()).isEqualTo(time)
        );
    }

    @DisplayName("모든 예약 시간을 조회할 수 있다")
    @Test
    void getReservationTimes() {
        // when
        List<ReservationTimeInfo> result = reservationTimeService.getReservationTimes();
        // then
        assertThat(result).hasSize(3);
    }

    @DisplayName("예약이 존재하는 시간을 삭제할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDeleteTimeWithinReservation() {
        // when
        // then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");
    }

    @DisplayName("id를 기반으로 예약 시간을 삭제할 수 있다")
    @Test
    void deleteReservationTimeById() {
        // when
        reservationTimeService.deleteReservationTimeById(3L);
        // then
        List<ReservationTime> times = reservationTimeDao.findAll();
        assertThat(times).hasSize(2);
    }

    @DisplayName("주어진 날짜와 테마에 시간 정보를 예약 가능 여부와 함께 조회할 수 있다")
    @Test
    void findAvailableTimes() {
        // when
        LocalDate date = LocalDate.of(2025, 4, 24);
        List<AvailableTimeInfo> result = reservationTimeService.findAvailableTimes(date, 7L);
        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).contains(
                        new AvailableTimeInfo(1L, LocalTime.of(10, 0), true),
                        new AvailableTimeInfo(2L, LocalTime.of(15, 0), false),
                        new AvailableTimeInfo(3L, LocalTime.of(16, 0), false)
                )
        );
    }
}
