package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.domain.repository.WaitingRepository;
import roomescape.support.ServiceIntegrationTest;

@DisplayName("대기 취소 → 순번 재정렬 트랜잭션 경계")
class WaitingCancelTransactionTest extends ServiceIntegrationTest {

    @Autowired
    private WaitingService waitingService;

    @MockitoSpyBean
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("대기 취소 후 순번 재정렬이 실패하면 취소(삭제)까지 전부 롤백된다")
    void 대기취소_재정렬_중간실패_전체롤백() {
        // given: 대기 [콘:1, 모카:2, 핀:3]
        LocalDate date = LocalDate.of(2050, 12, 31);
        Long timeId = fixture.insertTime(LocalTime.of(10, 0));
        Long themeId = fixture.insertTheme("테마A");
        Long conId = fixture.insertWaiting("콘", date, timeId, themeId, 1);
        Long mochaId = fixture.insertWaiting("모카", date, timeId, themeId, 2);
        Long pinId = fixture.insertWaiting("핀", date, timeId, themeId, 3);

        // [2] 재정렬 UPDATE에서 폭발 주입 (모카 취소 → 핀 3→2 당기는 그 UPDATE)
        willThrow(new RuntimeException("재정렬 실패 주입"))
                .given(waitingRepository).updateOrderIndex(anyLong(), anyInt());

        // when: 모카가 본인 대기 취소 → 삭제 후 재정렬에서 실패
        assertThatThrownBy(() -> waitingService.cancelByOwner(mochaId, "모카"))
                .isInstanceOf(RuntimeException.class);

        // then: 전부 롤백 — 모카 대기 그대로, 순번도 그대로
        assertThat(fixture.existsWaiting(mochaId)).isTrue();      // 삭제가 롤백됨
        assertThat(fixture.findWaitingOrder(conId)).isEqualTo(1);
        assertThat(fixture.findWaitingOrder(mochaId)).isEqualTo(2);
        assertThat(fixture.findWaitingOrder(pinId)).isEqualTo(3); // 당겨지지 않음
    }
}
