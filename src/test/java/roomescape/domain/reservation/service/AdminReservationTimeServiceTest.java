package roomescape.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.LocalTimeFixture.TEN_HOUR;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.reservation.dto.ReservationTimeAddRequest;
import roomescape.global.exception.EscapeApplicationException;

class AdminReservationTimeServiceTest {

    AdminReservationTimeService adminReservationTimeService;
    FakeReservationTimeRepository fakeReservationTimeRepository;

    @BeforeEach
    void setUp() {
        fakeReservationTimeRepository = new FakeReservationTimeRepository();
        adminReservationTimeService = new AdminReservationTimeService(fakeReservationTimeRepository);
    }

    @DisplayName("모든 예약 시간을 찾습니다")
    @Test
    void findAllReservationTime() {
        ReservationTime reservationTime = new ReservationTime(1L, TEN_HOUR);
        fakeReservationTimeRepository.insert(reservationTime);

        int actualSize = adminReservationTimeService.findAllReservationTime().size();

        assertThat(actualSize).isOne();
    }

    @DisplayName("예약시간을 추가하고 저장된 예약시간을 반환합니다.")
    @Test
    void should_add_reservation_time() {
        ReservationTime reservationTime = new ReservationTime(1L, TEN_HOUR);

        ReservationTime actualReservationTime = adminReservationTimeService.addReservationTime(
                new ReservationTimeAddRequest(TEN_HOUR));

        assertThat(actualReservationTime).isEqualTo(reservationTime);
    }

    @DisplayName("중복되는 예약 시각을 추가할 경우 예외가 발생합니다.")
    @Test
    void should_throw_ClientIllegalArgumentException_when_reservation_time_is_duplicated() {
        ReservationTime reservationTime = new ReservationTime(1L, TEN_HOUR);
        fakeReservationTimeRepository.insert(reservationTime);
        ReservationTimeAddRequest reservationTimeAddRequest = new ReservationTimeAddRequest(LocalTime.of(10, 0));

        assertThatThrownBy(() -> adminReservationTimeService.addReservationTime(reservationTimeAddRequest))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("이미 존재하는 예약시간은 추가할 수 없습니다.");
    }

    @DisplayName("원하는 id의 예약시간을 삭제합니다")
    @Test
    void should_remove_reservation_time_with_exist_id() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        fakeReservationTimeRepository.insert(reservationTime);

        adminReservationTimeService.removeReservationTime(1L);

        assertThat(fakeReservationTimeRepository.reservationTimes.containsKey(1L)).isFalse();
    }

    @DisplayName("없는 id의 예약시간을 삭제하면 예외를 발생합니다.")
    @Test
    void should_throw_ClientIllegalArgumentException_when_remove_reservation_time_with_non_exist_id() {
        assertThatThrownBy(() -> adminReservationTimeService.removeReservationTime(1L)).isInstanceOf(
                        EscapeApplicationException.class)
                .hasMessage("해당 id를 가진 예약시간이 존재하지 않습니다.");
    }
}
