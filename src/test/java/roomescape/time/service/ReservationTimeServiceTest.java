package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.common.CleanUp;
import roomescape.fixture.ReservationDbFixture;
import roomescape.fixture.ReservationTimeDbFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.time.controller.request.AvailableReservationTimeRequest;
import roomescape.time.controller.request.ReservationTimeCreateRequest;
import roomescape.time.controller.response.AvailableReservationTimeResponse;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;
    @Autowired
    private ReservationDbFixture reservationDbFixture;
    @Autowired
    private CleanUp cleanUp;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Test
    void 예약시간을_생성한다() {
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(
                LocalTime.of(10, 0)
        );

        ReservationTimeResponse response = reservationTimeService.open(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.startAt()).isEqualTo("10:00");
    }

    @Test
    void 이미_존재하는_시간은_추가할_수_없다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(reservationTime.getStartAt());

        assertThatThrownBy(() -> reservationTimeService.open(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 이미 존재하는 예약 시간입니다.");
    }

    @Test
    void 예약시간을_모두_조회한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();

        List<ReservationTimeResponse> responses = reservationTimeService.getAll();

        assertThat(responses.get(0).startAt()).isEqualTo(reservationTime.getStartAt().toString());
    }

    @Test
    void 예약시간을_삭제한다() {
        ReservationTime reservationTime = reservationTimeDbFixture.열시();

        reservationTimeService.deleteById(reservationTime.getId());

        assertThat(reservationTimeService.getAll()).hasSize(0);
    }

    @Test
    void 존재하지_않는_예약시간을_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("[ERROR] 예약 시간을 찾을 수 없습니다.");
    }

    @Test
    void 이미_해당_시간의_예약이_존재한다면_삭제할_수_없다() {
        Reservation reservation = reservationDbFixture.예약_유저1_내일_10시_공포();

        assertThatThrownBy(() -> reservationTimeService.deleteById(reservation.getReservationTime().getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 해당 시간에 이미 예약이 존재하여 삭제할 수 없습니다.");
    }

    @Test
    void 예약_가능한_시간을_조회한다() {
        Reservation reservation1 = reservationDbFixture.예약_유저1_내일_10시_공포();
        reservationDbFixture.예약_유저1_내일_11시_공포();
        AvailableReservationTimeRequest request = new AvailableReservationTimeRequest(
                reservation1.getDate(),
                reservation1.getTimeId()
        );

        List<AvailableReservationTimeResponse> responses = reservationTimeService.getAvailableReservationTimes(request);

        assertThat(responses.get(0).id()).isNotNull();
        assertThat(responses.get(0).startAt()).isEqualTo(reservation1.getReservationTime().getStartAt());
        assertThat(responses.get(0).isReserved()).isEqualTo(true);
        assertThat(responses.get(1).isReserved()).isEqualTo(false);
    }
}
