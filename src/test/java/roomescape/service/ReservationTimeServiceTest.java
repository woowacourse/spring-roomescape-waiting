package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.service.dto.ReservationTimeRequest;
import roomescape.service.dto.ReservationTimeResponse;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 시간을 성공적으로 추가한다")
    void addReservationTimeTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(18, 12));

        // when
        ReservationTimeResponse response = reservationTimeService.addReservationTime(reservationTimeRequest);

        // then
        assertAll(
                () -> assertThat(response.id()).isPositive(),
                () -> assertThat(response.startAt()).isEqualTo(LocalTime.of(18, 12))
        );
    }

    @Test
    @DisplayName("예약 시간을 삭제한다")
    void removeReservationTimeTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(12, 12));
        final ReservationTimeResponse reservationTimeResponse = reservationTimeService.addReservationTime(
                reservationTimeRequest);
        final long id = reservationTimeResponse.id();

        // when, then
        assertThatCode(() -> reservationTimeService.removeReservationTime(id))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 시간이 예약에 사용되고 있다면 예외가 발생한다")
    void removeReferencedReservationTimeTest() {
        // given
        final long id = 1L;

        // when, then
        assertThatThrownBy(() -> reservationTimeService.removeReservationTime(id))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("모든 예약 시간을 검색한다")
    void findReservationTimesTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(12, 12));
        reservationTimeService.addReservationTime(reservationTimeRequest);

        // when
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.findReservationTimes();

        // then
        assertThat(reservationTimes).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하여 예외가 발생한다.")
    void deleteByIdTest() {
        //given
        final long id = Long.MAX_VALUE;

        //should
        assertThatThrownBy(() -> reservationTimeService.removeReservationTime(id)).isInstanceOf(
                NoSuchElementException.class);

    }
}
