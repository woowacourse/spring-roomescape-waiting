package roomescape.reservation.service;

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
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.response.ReservationTimeWithIsBookedGetResponse;
import roomescape.reservation.exception.DuplicateTimeException;
import roomescape.global.exception.InvalidInputException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약시간을 추가를 할 수 있다.")
    void createReservationTime() {
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(11, 0));
        ReservationTime actual = reservationTimeService.createReservationTime(request);
        assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(6L);
            assertThat(actual.getStartAt()).isEqualTo(LocalTime.of(11, 0));
        });
    }

    @Test
    @DisplayName("이미 존재하는 시간에는 예약시간을 추가를 할 수 없다.")
    void cannotCreateReservationTime() {
        // Given
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));

        // When & Then
        assertThatThrownBy(() -> reservationTimeService.createReservationTime(request))
                .isInstanceOf(DuplicateTimeException.class);
    }

    @Test
    @DisplayName("모든 예약시간 정보를 가져올 수 있다.")
    void findAllReservationTimes() {
        assertThat(reservationTimeService.findAllReservationTimes()).hasSize(5);
    }

    @Test
    @DisplayName("주어진 날짜와 테마id에 해당하는 예약시간 및 예약여부를 조회한다")
    void findReservationTimeByDateAndThemeIdWithIsBooked() {
        // Given
        // When
        List<ReservationTimeWithIsBookedGetResponse> responses = reservationTimeService.findReservationTimeByDateAndThemeIdWithIsBooked(LocalDate.of(2025, 5, 1), 1L);

        // Then
        assertAll(() -> {
            assertThat(responses.size()).isEqualTo(5);
            assertThat(responses.get(0).isBooked()).isTrue();
            assertThat(responses.get(1).isBooked()).isFalse();
            assertThat(responses.get(2).isBooked()).isFalse();
            assertThat(responses.get(3).isBooked()).isFalse();
            assertThat(responses.get(4).isBooked()).isFalse();
        });
    }

    @Test
    @DisplayName("예약시간을 id를 통해 제거할 수 있다.")
    void deleteReservationTimeById() {
        reservationTimeService.deleteReservationTimeById(4L);
        assertThat(reservationTimeService.findAllReservationTimes()).hasSize(4);
    }

    @Test
    @DisplayName("존재하지 않는 예약시간 id로는 제거할 수 없다.")
    void cannotDeleteReservationTimeById() {
        // Given
        // When
        // Then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(500L))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("존재하지 않는 예약시간 id이다.");
    }
}
