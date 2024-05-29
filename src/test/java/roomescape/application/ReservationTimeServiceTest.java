package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.BasicAcceptanceTest;
import roomescape.TestFixtures;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.dto.AvailableTimeResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.exception.RoomescapeException;

class ReservationTimeServiceTest extends BasicAcceptanceTest {
    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("요청으로 들어온 예약 시간이 예외 조건에 해당되지 않을 때 해당 예약 시간을 저장한다.")
    @Test
    void save() {
        reservationTimeService.save(TestFixtures.RESERVATION_TIME_REQUEST);
        List<ReservationTimeResponse> reservationTimeResponses = reservationTimeService.findAll();

        assertThat(reservationTimeResponses).isEqualTo(TestFixtures.RESERVATION_TIME_RESPONSES_1);
    }

    @DisplayName("이미 존재하는 예약 시간을 생성 요청하면 예외가 발생한다.")
    @Test
    void duplicateTime() {
        ReservationTime reservationTime = reservationTimeRepository.findAll().get(0);
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(reservationTime.getStartAt());

        assertThatThrownBy(() -> reservationTimeService.save(reservationTimeRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(String.format("중복된 예약 시간입니다. 요청 예약 시간:%s", reservationTimeRequest.startAt()));
    }

    @DisplayName("해당 id의 예약 시간을 삭제한다.")
    @Test
    void deleteById() {
        reservationTimeService.deleteById(4L);
        List<ReservationTimeResponse> reservationTimeResponses = reservationTimeService.findAll();

        assertThat(reservationTimeResponses).isEqualTo(TestFixtures.RESERVATION_TIME_RESPONSES_2);
    }

    @DisplayName("예약에 사용된 예약 시간을 삭제 요청하면, 예외가 발생한다.")
    @Test
    void invalidReservationWhenReservedInTime() {
        ReservationTime reservationTime = reservationTimeRepository.findAll().get(0);

        assertThatThrownBy(() -> reservationTimeService.deleteById(reservationTime.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(String.format("해당 예약 시간에 연관된 예약이 존재하여 삭제할 수 없습니다. 삭제 요청한 시간:%s", reservationTime.getStartAt()));
    }

    @DisplayName("존재하지 않는 예약 시간을 삭제 요청하면, 예외가 발생한다.")
    @Test
    void invalidNotExistTime() {
        assertThatThrownBy(() -> reservationTimeService.deleteById(99L))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }

    @DisplayName("예약이 있는 시간과 없는 시간을 구분한다.")
    @Test
    void findAvailableTimes() {
        List<AvailableTimeResponse> availableTimeResponses = reservationTimeService.findAvailableTimes(
                LocalDate.of(2099, 4, 29), 1L
        );

        assertThat(availableTimeResponses).isEqualTo(TestFixtures.AVAILABLE_TIME_RESPONSES);
    }
}
