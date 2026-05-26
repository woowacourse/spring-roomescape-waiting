package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("전체 예약 시간 목록을 반환한다")
    void findAll() {
        // when
        List<ReservationTimeResponse> result = reservationTimeService.findAll();

        // then
        assertThat(result).hasSize(9);
    }

    @Test
    @DisplayName("전체 예약 시간이 10:00부터 18:00까지 순서대로 존재한다")
    void findAll_containsExpectedTimes() {
        // when
        List<ReservationTimeResponse> result = reservationTimeService.findAll();

        // then
        List<LocalTime> startTimes = result.stream()
                .map(ReservationTimeResponse::startAt)
                .toList();

        assertThat(startTimes).containsExactly(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                LocalTime.of(17, 0),
                LocalTime.of(18, 0)
        );
    }

    @Test
    @DisplayName("이미 존재하지 않는 시간대는 정상적으로 저장된다")
    void save() {
        // given
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(19, 0));

        // when
        ReservationTimeResponse result = reservationTimeService.save(request);

        // then
        assertThat(result.startAt()).isEqualTo(LocalTime.of(19, 0));
        assertThat(reservationTimeService.findAll()).hasSize(10);
    }

    @Test
    @DisplayName("이미 존재하는 시간대를 저장하면 예외를 던진다")
    void save_throwsException_whenDuplicateStartAt() {
        // given
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));

        // when & then
        assertThatThrownBy(() -> reservationTimeService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 시간대이므로 추가할 수 없습니다.");
    }

    @Test
    @DisplayName("예약이 없는 시간대는 삭제할 수 있다")
    void delete() {
        // given
        Long timeIdWithNoReservation = 6L;

        // when
        reservationTimeService.delete(timeIdWithNoReservation);

        // then
        assertThat(reservationTimeService.findAll()).hasSize(8);
    }

    @Test
    @DisplayName("예약이 존재하는 시간대를 삭제하려하면 예외를 던진다")
    void delete_throwsException_whenReservationExists() {
        // given
        Long timeIdWithReservation = 1L;

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(timeIdWithReservation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
    }
}
