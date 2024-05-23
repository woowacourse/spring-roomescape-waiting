package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.IntegrationTestSupport;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dto.request.ReservationTimeBookedRequest;
import roomescape.service.dto.request.ReservationTimeSaveRequest;
import roomescape.service.dto.response.ReservationTimeBookedResponses;
import roomescape.service.dto.response.ReservationTimeResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
class ReservationTimeServiceTest extends IntegrationTestSupport {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @DisplayName("예약 시간 목록 조회")
    @Test
    void getTimes() {
        List<ReservationTimeResponse> reservationTimeResponses = reservationTimeService.getTimes().reservationTimeResponses();
        assertThat(reservationTimeResponses).hasSize(7);
    }

    @DisplayName("예약 시간 추가")
    @Test
    void saveTime() {
        ReservationTimeSaveRequest reservationTimeSaveRequest = new ReservationTimeSaveRequest(
                LocalTime.parse("01:00"));
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.saveTime(
                reservationTimeSaveRequest);
        assertThat(reservationTimeResponse.startAt()).isEqualTo(LocalTime.parse("01:00"));
    }

    @DisplayName("예약 시간 삭제")
    @Test
    void deleteTime() {
        int size = reservationTimeService.getTimes().reservationTimeResponses().size();

        reservationTimeService.deleteTime(6L);
        assertThat(reservationTimeService.getTimes().reservationTimeResponses()).hasSize(size - 1);
    }

    @DisplayName("존재하지 않는 예약 시간 삭제")
    @Test
    void deleteTimeNotFound() {
        assertThatThrownBy(() -> reservationTimeService.deleteTime(3L))
                .isInstanceOf(RoomEscapeBusinessException.class);
    }

    @DisplayName("예약이 존재하는 시간 삭제")
    @Test
    void deleteTimeExistReservation() {
        assertThatThrownBy(() -> reservationTimeService.deleteTime(2L))
                .isInstanceOf(RoomEscapeBusinessException.class);
    }

    @DisplayName("중복된 시간 저장")
    @Test
    void saveDuplicatedTime() {
        ReservationTimeSaveRequest reservationTimeSaveRequest = new ReservationTimeSaveRequest(
                LocalTime.parse("11:00"));

        assertThatThrownBy(
                () -> reservationTimeService.saveTime(reservationTimeSaveRequest))
                .isInstanceOf(RoomEscapeBusinessException.class);
    }

    @DisplayName("시간의 예약 여부 조회")
    @Test
    void findBookedTimes() {
        // given
        var reservationTimeBookedRequest = new ReservationTimeBookedRequest(LocalDate.parse("2024-05-04"), 1L);

        // when
        ReservationTimeBookedResponses timesWithBooked = reservationTimeService.getTimesWithBooked(
                reservationTimeBookedRequest);

        // then
        assertThat(timesWithBooked.reservationTimeBookedResponses())
                .hasSize(7)
                .extracting("startAt", "alreadyBooked")
                .containsExactly(
                        tuple(LocalTime.parse("09:00"), true),
                        tuple(LocalTime.parse("10:00"), true),
                        tuple(LocalTime.parse("11:00"), false),
                        tuple(LocalTime.parse("12:00"), false),
                        tuple(LocalTime.parse("13:00"), false),
                        tuple(LocalTime.parse("14:00"), false),
                        tuple(LocalTime.parse("15:00"), false)
                );
    }
}

