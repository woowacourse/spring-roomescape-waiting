package roomescape.service;

import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import roomescape.fake.FakeReservationTimeDao;
import roomescape.dto.request.ReservationTimeRegisterDto;
import roomescape.dto.response.ReservationTimeResponseDto;

class ReservationTimeServiceTest {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeServiceTest() {
        this.reservationTimeService = new ReservationTimeService(new FakeReservationTimeDao());
    }

    @Test
    void 시간을_저장한다() {
        // given
        ReservationTimeRegisterDto request = new ReservationTimeRegisterDto(LocalTime.of(10, 0).toString());

        // when
        ReservationTimeResponseDto response = reservationTimeService.saveTime(request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 모든_시간을_조회한다() {
        // given
        reservationTimeService.saveTime(new ReservationTimeRegisterDto(LocalTime.of(10, 0).toString()));
        reservationTimeService.saveTime(new ReservationTimeRegisterDto(LocalTime.of(12, 0).toString()));

        // when
        List<ReservationTimeResponseDto> times = reservationTimeService.getAllTimes();

        // then
        assertThat(times).hasSize(2);
        assertThat(times).extracting("startAt")
                .containsExactlyInAnyOrder(LocalTime.of(10, 0), LocalTime.of(12, 0));
    }

    @Test
    void 시간을_삭제한다() {
        // given
        ReservationTimeResponseDto saved = reservationTimeService.saveTime(
                new ReservationTimeRegisterDto(LocalTime.of(10, 0).toString())
        );

        // when
        reservationTimeService.deleteTime(saved.id());

        // then
        List<ReservationTimeResponseDto> times = reservationTimeService.getAllTimes();
        assertThat(times).isEmpty();
    }

}
