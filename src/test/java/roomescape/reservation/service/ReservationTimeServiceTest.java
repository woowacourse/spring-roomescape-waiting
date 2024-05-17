package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.controller.dto.request.ReservationTimeSaveRequest;
import roomescape.reservation.controller.dto.response.ReservationTimeDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationTimeResponse;
import roomescape.reservation.repository.ReservationTimeRepository;

@SpringBootTest
@Transactional
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("예약 가능 시간을 성공적으로 저장한다.")
    @Test
    void saveReservationTime() {
        // given
        ReservationTimeSaveRequest saveRequest = new ReservationTimeSaveRequest(LocalTime.parse("16:00"));

        // when
        ReservationTimeResponse response = reservationTimeService.save(saveRequest);

        // then
        assertNotNull(response);
        assertEquals(LocalTime.parse("16:00"), response.startAt());
    }

    @DisplayName("모든 예약 가능 시간을 조회한다.")
    @Test
    void getAllReservationTimes() {
        // when
        List<ReservationTimeResponse> responses = reservationTimeService.getAll();

        // then
        assertNotNull(responses);
        assertEquals(4, responses.size());
        assertThat(responses).extracting(ReservationTimeResponse::startAt)
                .containsExactlyInAnyOrder(
                        LocalTime.parse("10:00"), LocalTime.parse("23:00"),
                        LocalTime.parse("14:00"), LocalTime.parse("20:00")
                );
    }

    @DisplayName("예약 가능 시간 ID로 예약 가능 시간을 삭제한다.")
    @Test
    void deleteReservationTime() {
        // when
        ReservationTimeDeleteResponse response = reservationTimeService.delete(4L);

        // then
        assertNotNull(response);
        assertEquals(1, response.updateCount());
    }

    @DisplayName("이미 예약이 존재하는 시간을 삭제 시도할 시 실패한다.")
    @Test
    void deleteAlreadyReservedReservationTimeThrowsException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> reservationTimeService.delete(1L));
    }

    @DisplayName("존재하지 않는 예약 가능 시간 ID로 삭제 시 예외를 던진다.")
    @Test
    void deleteNonExistingReservationTimeThrowsException() {
        // given
        long nonExistingId = 999L;

        // when & then
        assertThrows(NoSuchElementException.class, () -> reservationTimeService.delete(nonExistingId));
    }
}
