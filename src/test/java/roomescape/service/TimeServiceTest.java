package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dao.fake.FakeReservationTimeDao;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.TimeRequest;
import roomescape.exception.custom.DuplicatedException;

class TimeServiceTest {

    private TimeService timeService;

    @BeforeEach
    void setUp() {
        timeService = new TimeService(new FakeReservationTimeDao());
    }

    @Test
    @DisplayName("모든 예약 시간 정보를 가져올 수 있다.")
    void findAllReservationTimes() {
        TimeRequest request1 = new TimeRequest(LocalTime.of(10, 0));
        TimeRequest request2 = new TimeRequest(LocalTime.of(12, 0));

        timeService.addReservationTime(request1);
        timeService.addReservationTime(request2);

        assertThat(timeService.findAllReservationTimes()).hasSize(2);
    }

    @Test
    @DisplayName("예약 시간을 추가를 할 수 있다.")
    void addReservationTime() {
        TimeRequest request = new TimeRequest(LocalTime.of(10, 0));

        ReservationTime actual = timeService.addReservationTime(request);

        assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getStartAt()).isEqualTo(LocalTime.of(10, 0));
        });
    }

    @Test
    @DisplayName("예약 시간이 이미 존재한다면 추가할 수 없다.")
    void addDuplicatedReservationTime() {
        TimeRequest request = new TimeRequest(LocalTime.of(10, 0));

        timeService.addReservationTime(request);

        assertThatThrownBy(() -> timeService.addReservationTime(request))
            .isInstanceOf(DuplicatedException.class)
            .hasMessageContaining("reservationTime");
    }

    @Test
    @DisplayName("예약 시간을 id를 통해 제거할 수 있다.")
    void removeReservationTime() {
        TimeRequest request = new TimeRequest(LocalTime.of(10, 0));
        timeService.addReservationTime(request);

        timeService.removeReservationTime(1L);

        assertThat(timeService.findAllReservationTimes()).hasSize(0);
    }
}
