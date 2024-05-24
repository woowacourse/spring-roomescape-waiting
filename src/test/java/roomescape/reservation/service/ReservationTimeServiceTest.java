package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.InitialReservationFixture.RESERVATION_1;
import static roomescape.InitialReservationTimeFixture.INITIAL_RESERVATION_TIME_COUNT;
import static roomescape.InitialReservationTimeFixture.NOT_RESERVATED_TIME;
import static roomescape.InitialReservationTimeFixture.RESERVATION_TIME_1;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exceptions.ValidationException;
import roomescape.reservation.dto.request.ReservationTimeRequest;
import roomescape.reservation.dto.response.ReservationTimeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("중복된 시간을 저장하려고 하면 예외가 발생한다.")
    void saveDuplicatedGetTime() {
        assertThatThrownBy(
                () -> reservationTimeService.addTime(new ReservationTimeRequest(RESERVATION_TIME_1.getStartAt()))
        ).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("예약 가능 시간을 추가하고 id값을 붙여서 응답 DTO를 생성한다.")
    void addGetTime() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 0));

        ReservationTimeResponse reservationTimeResponse = reservationTimeService.addTime(reservationTimeRequest);

        assertThat(reservationTimeResponse.id()).isNotNull();
    }

    @Test
    @DisplayName("모든 예약 가능 시간을 조회한다.")
    void getTimes() {
        List<ReservationTimeResponse> times = reservationTimeService.findReservationTimes();

        assertThat(times).hasSize(INITIAL_RESERVATION_TIME_COUNT);
    }

    @Test
    @DisplayName("id에 맞는 예약 가능 시간을 조회한다.")
    void getGetTime() {
        ReservationTimeResponse timeResponse = reservationTimeService.getReservationTimeById(RESERVATION_1.getId());

        assertThat(timeResponse.startAt()).isEqualTo(RESERVATION_1.getReservationTime().getStartAt().toString());
    }

    @Test
    @DisplayName("id에 맞는 예약 가능 시간을 삭제한다.")
    void deleteGetTime() {
        reservationTimeService.deleteTime(NOT_RESERVATED_TIME.getId());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_time", Integer.class);

        assertThat(count).isEqualTo(INITIAL_RESERVATION_TIME_COUNT - 1);
    }
}
