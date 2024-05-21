package roomescape.time.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.reservation.repository.FakeReservationRepository;
import roomescape.time.domain.Time;
import roomescape.time.dto.TimeRequest;
import roomescape.time.dto.TimeResponse;
import roomescape.time.exception.TimeExceptionCode;
import roomescape.time.repository.FakeTimeRepository;

class TimeServiceTest {

    private static final LocalTime CURRENT_TIME = LocalTime.of(9, 0);
    private static final Time TIME = Time.from(LocalTime.of(17, 3));

    private final TimeService timeService;

    public TimeServiceTest() {
        this.timeService = new TimeService(new FakeTimeRepository(), new FakeReservationRepository());
    }

    @Test
    @DisplayName("시간을 추가한다.")
    void addReservationTime() {
        TimeRequest timeRequest = new TimeRequest(TIME.getStartAt());
        TimeResponse timeResponse = timeService.addReservationTime(timeRequest);

        assertAll(
                () -> assertEquals(timeResponse.id(), 3),
                () -> assertEquals(timeResponse.startAt(), TIME.getStartAt())
        );
    }

    @Test
    @DisplayName("시간을 찾는다.")
    void findReservationTimes() {
        List<TimeResponse> timeResponses = timeService.findReservationTimes();

        Assertions.assertThat(timeResponses.size())
                .isEqualTo(2);
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

    @Test
    @DisplayName("예약이 존재하는 예약 시간 삭제 요청시 예외를 던진다.")
    void validateReservationExistence_ShouldThrowException_WhenReservationExistAtTime() {
        Throwable reservationExistAtTime = assertThrows(
                RoomEscapeException.class,
                () -> timeService.removeReservationTime(TIME.getId()));

        assertEquals(TimeExceptionCode.EXIST_RESERVATION_AT_CHOOSE_TIME.getMessage(),
                reservationExistAtTime.getMessage());
    }

}
