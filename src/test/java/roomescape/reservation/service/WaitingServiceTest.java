package roomescape.reservation.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.fake.FakeWaitingRepository;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.application.service.WaitingService;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.domain.repository.dto.WaitingDetail;

class WaitingServiceTest {

    private WaitingRepository waitingRepository;
    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        waitingService = new WaitingService(waitingRepository);
    }

    @DisplayName("대기 취소를 할 수 있어야 한다.")
    @Test
    void cancel_user_waiting() {
        Waiting waiting = Waiting.of(null, "타스", LocalDate.of(2026, 5, 27), 1L, 1L);
        Waiting saved = waitingRepository.save(waiting);

        waitingService.delete(saved.getId(), "타스");
        Optional<WaitingDetail> deletedWaiting = waitingRepository.findDetailById(saved.getId());

        Assertions.assertThat(deletedWaiting.isPresent()).isFalse();
    }

    @DisplayName("취소하려는 대기 ID의 예약자 명이 다르다면 예외를 발생시켜야 한다.")
    @Test
    void exception_when_name_is_unmatched() {
        Waiting waiting = Waiting.of(null, "타스", LocalDate.of(2026, 5, 27), 1L, 1L);
        Waiting saved = waitingRepository.save(waiting);

        assertThatThrownBy(() -> waitingService.delete(saved.getId(), "카야"))
                .isExactlyInstanceOf(RoomEscapeException.class)
                .hasMessage(ReservationErrorCode.FORBIDDEN_RESERVATION_ACCESS.message());
    }
}
