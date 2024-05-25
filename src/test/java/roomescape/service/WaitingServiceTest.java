package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.IntegrationTestSupport;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dto.request.WaitingRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaitingServiceTest extends IntegrationTestSupport {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private WaitingService waitingService;

    @Test
    @DisplayName("예약 대기를 저장할 수 있다")
    void saveWaiting() {
        int beforeSize = waitingRepository.findAll().size();
        WaitingRequest waitingRequest = new WaitingRequest(LocalDate.now(), 1L, 1L);

        waitingService.saveWaiting(waitingRequest, 1L);

        int afterSize = waitingRepository.findAll().size();
        assertThat(afterSize).isEqualTo(beforeSize + 1);
    }

    @Test
    @DisplayName("예약 대기를 삭제할 수 있다")
    void deleteWaiting() {
        int beforeSize = waitingRepository.findAll().size();

        waitingService.deleteWaiting(1L);

        int afterSize = waitingRepository.findAll().size();
        assertThat(afterSize).isEqualTo(beforeSize - 1);
    }

    @Test
    @DisplayName("유저는 본인 예약대기가 아니면 삭제할 수 없다")
    void should_ThrowRoomBusinessException_WhenTryDeleteOtherUserWaiting() {
        assertThatThrownBy(() -> waitingService.deleteUserWaiting(3L, 1L))
                .isInstanceOf(RoomEscapeBusinessException.class);
    }

    @Test
    @DisplayName("모든 예약 대기를 가져올 수 있다")
    void findAllWaitings() {
        int beforeSize = waitingRepository.findAll().size();
        int actualSize = waitingService.findAllWaitings().waitingResponses().size();

        assertThat(actualSize).isEqualTo(beforeSize);
    }
}
