package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.DuplicateWaitingException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.WaitingCommand;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;

    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        waitingService = new WaitingService(waitingRepository);
    }

    @Test
    @DisplayName("존재하는 예약 대기를 삭제한다.")
    void deleteWaiting() {
        WaitingCommand waiting = new WaitingCommand("브라운", LocalDate.now(), 1L, 1L);
        given(waitingRepository.calculateWaitingNumber(waiting)).willReturn(1);

        assertThatCode(() -> waitingService.removeWaiting(waiting)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 예약 대기를 삭제하면, 예외가 발생한다.")
    void deleteNotExistsWaiting() {
        WaitingCommand waiting = new WaitingCommand("브라운", LocalDate.now(), 1L, 1L);
        given(waitingRepository.calculateWaitingNumber(waiting)).willReturn(0);

        assertThatThrownBy(() -> waitingService.removeWaiting(waiting)).isInstanceOf(WaitingNotFoundException.class);
    }

    @Test
    @DisplayName("존재하는 예약 대기를 중복해서 저장하면, 예외가 발생한다.")
    void saveDuplicateWaiting() {
        WaitingCommand waiting = new WaitingCommand("브라운", LocalDate.now(), 1L, 1L);
        waitingService.saveWaiting(waiting);
        given(waitingRepository.isExists(waiting)).willReturn(true);

        assertThatThrownBy(() -> waitingService.saveWaiting(waiting)).isInstanceOf(DuplicateWaitingException.class);
    }
}
