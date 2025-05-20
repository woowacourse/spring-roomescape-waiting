package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 시간을 성공적으로 추가한다")
    void addReservationTimeTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(12, 12));

        // when
        ReservationTimeResponse response = reservationTimeService.addReservationTime(reservationTimeRequest);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.startAt()).isEqualTo(LocalTime.of(12, 12))
        );
    }

    @Test
    @DisplayName("예약 시간을 삭제한다")
    void removeReservationTimeTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(12, 12));
        reservationTimeService.addReservationTime(reservationTimeRequest);
        Long id = 1L;

        // when, then
        assertThatCode(() -> reservationTimeService.removeReservationTime(id))
                .doesNotThrowAnyException();
    }

    // TODO : 수정 하기
    @Test
    @DisplayName("예약 시간이 예약에 사용되고 있다면 예외가 발생한다")
    @Disabled
    void removeReferencedReservationTimeTest() {
        // given
        Long id = 1L;

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
        assertThat(reservationTimes).hasSize(1);
    }
}
