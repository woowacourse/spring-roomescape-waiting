package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.ReservationStatus;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ReservationTimeStatusResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeServiceTest {

    public static final int DEFALUT_TIME_COUNT = 9;
    public static final Long AVAILABLE_TIME_ID = 1L;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    void 전체_예약_시간_목록_조회() {
        List<ReservationTimeResponse> result = reservationTimeService.findAll();

        assertThat(result).hasSize(DEFALUT_TIME_COUNT);
    }

    @Test
    void 전체_예약_시간_순서_확인() {
        List<ReservationTimeResponse> result = reservationTimeService.findAll();

        List<LocalTime> startTimes = result.stream().map(ReservationTimeResponse::startAt).toList();

        assertThat(startTimes).containsExactly(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                LocalTime.of(17, 0),
                LocalTime.of(18, 0));
    }

    @Test
    void 중복되지_않는_시간_저장() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(19, 0));

        ReservationTimeResponse result = reservationTimeService.save(request);

        assertThat(result.startAt()).isEqualTo(LocalTime.of(19, 0));
        assertThat(reservationTimeService.findAll()).hasSize(DEFALUT_TIME_COUNT + 1);
    }

    @Test
    void 중복_시간_저장_시_예외() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));

        assertThatThrownBy(() -> reservationTimeService.save(request)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 시간대이므로 추가할 수 없습니다.");
    }

    @Test
    void 예약_없는_시간_삭제() {
        Long timeIdWithNoReservation = 6L;

        reservationTimeService.delete(timeIdWithNoReservation);

        assertThat(reservationTimeService.findAll()).hasSize(DEFALUT_TIME_COUNT - 1);
    }

    @Test
    void 예약_존재하는_시간_삭제_시_예외() {
        Long timeIdWithReservation = 1L;

        assertThatThrownBy(() -> reservationTimeService.delete(timeIdWithReservation)).isInstanceOf(
                IllegalArgumentException.class).hasMessage("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
    }

    @Test
    void 예약된_시간_제외_가용_시간_조회() {
        String date = LocalDate.now().minusDays(6).toString();

        List<ReservationTimeStatusResponse> result = reservationTimeService.findAvailableTime(AVAILABLE_TIME_ID, date);

        long availableCount = result.stream()
                .filter(r -> r.status() == ReservationStatus.AVAILABLE).count();

        assertThat(availableCount).isEqualTo(4);
    }

    @Test
    void 예약_없는_날짜의_전체_가용_시간_조회() {
        String date = LocalDate.now().plusDays(30).toString();

        List<ReservationTimeStatusResponse> result = reservationTimeService.findAvailableTime(AVAILABLE_TIME_ID, date);

        assertThat(result).hasSize(DEFALUT_TIME_COUNT);
    }
}
