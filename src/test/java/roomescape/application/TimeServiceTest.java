package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.TimeRequest;
import roomescape.domain.Time;
import roomescape.domain.repository.TimeQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@ServiceTest
class TimeServiceTest {

    @Autowired
    private TimeService timeService;

    @Autowired
    private TimeQueryRepository timeQueryRepository;

    @DisplayName("이미 존재하는 예약 시간을 생성 요청하면 예외가 발생한다.")
    @Test
    void shouldThrowsIllegalStateExceptionWhenCreateExistStartAtTime() {
        Time time = timeQueryRepository.findAll().get(0);
        TimeRequest request = new TimeRequest(time.getStartAt());

        assertThatCode(() -> timeService.create(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.DUPLICATED_TIME);
    }

    @DisplayName("예약에 사용된 예약 시간을 삭제 요청하면, 예외가 발생한다.")
    @Test
    void shouldThrowsExceptionReservationWhenReservedInTime() {
        Time time = timeQueryRepository.findAll().get(0);

        assertThatCode(() -> timeService.deleteById(time.getId()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.ALREADY_RESERVED);
    }

    @DisplayName("존재하지 않는 예약 시간을 삭제 요청하면, IllegalArgumentException 예외가 발생한다.")
    @Test
    void shouldThrowsIllegalArgumentExceptionWhenReservationTimeDoesNotExist() {
        assertThatCode(() -> timeService.deleteById(99L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_TIME);
    }
}
