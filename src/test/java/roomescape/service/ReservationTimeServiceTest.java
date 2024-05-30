package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.exception.time.DuplicatedTimeException;
import roomescape.helper.fixture.DateFixture;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.response.AvailableReservationTimeResponse;
import roomescape.service.dto.response.ReservationTimeResponse;

class ReservationTimeServiceTest extends ServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    void 모든_예약_시간을_조회할_수_있다() {
        List<ReservationTimeResponse> responses = reservationTimeService.findAllReservationTime();

        assertThat(responses.size()).isEqualTo(1);
    }

    @Test
    void 예약_가능한_시간을_조회할_수_있다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('12:00')");
        List<AvailableReservationTimeResponse> responses = reservationTimeService.findAllAvailableReservationTime(
                DateFixture.tomorrow(), 1);

        assertAll(
                () -> assertThat(responses.get(0).alreadyBooked()).isTrue(),
                () -> assertThat(responses.get(1).alreadyBooked()).isFalse()
        );
    }

    @Test
    void 예약_시간을_저장할_수_있다() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(12, 0));
        ReservationTimeResponse response = reservationTimeService.saveReservationTime(request);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void 중복된_시간_예약은_할_수_없다() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(12, 0));
        ReservationTimeResponse response = reservationTimeService.saveReservationTime(request);

        assertThatThrownBy(() -> reservationTimeService.saveReservationTime(request))
                .isInstanceOf(DuplicatedTimeException.class);
    }

    @Test
    void 예약_시간을_삭제할_수_있다() {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", 1);
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", 2);
        jdbcTemplate.update("DELETE FROM waiting WHERE id = ?", 1);

        reservationTimeService.deleteReservationTime(1);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_time", Integer.class);

        assertThat(count).isEqualTo(0);
    }
}