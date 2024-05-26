package roomescape.domain.time.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ServiceTest;
import roomescape.domain.time.domain.ReservationTime;
import roomescape.domain.time.dto.ReservationTimeAddRequest;
import roomescape.global.exception.DataConflictException;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminReservationTimeServiceTest extends ServiceTest {

    @Autowired
    AdminReservationTimeService adminReservationTimeService;

    @DisplayName("모든 예약 시간을 찾습니다")
    @Test
    void findAllReservationTime() {
        List<ReservationTime> reservationTimes = adminReservationTimeService.findAllReservationTime();

        assertThat(reservationTimes).hasSize(5);
    }

    @DisplayName("예약시간을 추가하고 저장된 예약시간을 반환합니다.")
    @Test
    void should_add_reservation_time() {
        ReservationTime actualReservationTime = adminReservationTimeService.addReservationTime(
                new ReservationTimeAddRequest(LocalTime.of(10, 0)));

        ReservationTime expectedReservationTime = new ReservationTime(6L, LocalTime.of(10, 0));

        assertThat(actualReservationTime.getId()).isEqualTo(expectedReservationTime.getId());
    }

    @DisplayName("중복되는 예약 시각을 추가할 경우 예외가 발생합니다.")
    @Test
    void should_throw_DataConflictException_when_reservation_time_is_duplicated() {
        ReservationTimeAddRequest reservationTimeAddRequest = new ReservationTimeAddRequest(LocalTime.of(11, 0));

        assertThatThrownBy(() -> adminReservationTimeService.addReservationTime(reservationTimeAddRequest))
                .isInstanceOf(DataConflictException.class)
                .hasMessage("이미 존재하는 예약시간은 추가할 수 없습니다.");
    }

    @DisplayName("원하는 id의 예약시간을 삭제합니다")
    @Test
    void should_remove_reservation_time_with_exist_id() {
        adminReservationTimeService.removeReservationTime(5L);

        assertThat(adminReservationTimeService.findAllReservationTime()).hasSize(4);
    }

    @DisplayName("없는 id의 예약시간을 삭제하면 예외를 발생합니다.")
    @Test
    void should_throw_EntityNotFoundException_when_remove_reservation_time_with_non_exist_id() {
        assertThatThrownBy(() -> adminReservationTimeService.removeReservationTime(6L)).isInstanceOf(
                        EntityNotFoundException.class)
                .hasMessage("해당 id를 가진 예약시간이 존재하지 않습니다.");
    }
}
