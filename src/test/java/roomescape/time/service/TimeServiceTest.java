package roomescape.time.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.time.domain.Time;
import roomescape.time.dto.TimeRequest;
import roomescape.time.exception.TimeExceptionCode;
import roomescape.time.exception.model.TimeNotFoundException;
import roomescape.time.repository.FakeTimeRepository;

class TimeServiceTest {

    private static final LocalTime CURRENT_TIME = LocalTime.of(9, 0);
    private static final Time TIME = Time.from(LocalTime.of(17, 3));

    private final TimeService timeService;

    public TimeServiceTest() {
        this.timeService = new TimeService(new FakeTimeRepository());
    }

    @Test
    @DisplayName("시간을 추가한다.")
    void addReservationTime() {
        TimeRequest timeRequest = new TimeRequest(TIME.getStartAt());
        Time time = timeService.addReservationTime(timeRequest);

        assertEquals(time.getStartAt(), timeRequest.startAt());
    }

    @Test
    @DisplayName("시간을 찾는다.")
    void findReservationTimes() {
        assertDoesNotThrow(timeService::findReservationTimes);
    }

    @Test
    @DisplayName("존재하는 시간이 없을 경우 에러가 발생한다.")
    void notExistTimeReservation() {
        Throwable notExistTime = assertThrows(TimeNotFoundException.class,
                () -> timeService.findTime(100));
        assertEquals(notExistTime.getMessage(), new TimeNotFoundException().getMessage());
    }

    @Test
    @DisplayName("중복된 예약 시간 생성 요청시 예외를 던진다.")
    void validation_ShouldThrowException_WhenStartAtIsDuplicated() {
        Throwable duplicateStartAt = assertThrows(RoomEscapeException.class,
                () -> timeService.addReservationTime(new TimeRequest(CURRENT_TIME)));

        assertEquals(TimeExceptionCode.DUPLICATE_TIME_EXCEPTION.getMessage(), duplicateStartAt.getMessage());
    }

    @Test
    @DisplayName("시간을 지운다.")
    void removeReservationTime() {
        assertDoesNotThrow(() -> timeService.removeReservationTime(1L));
    }
}
