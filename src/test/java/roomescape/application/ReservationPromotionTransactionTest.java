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

@DisplayName("예약 취소 → 자동 승격 트랜잭션 경계")
class ReservationPromotionTransactionTest extends ServiceIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    // @MockBean이 아니라 @MockitoSpyBean: 실제 빈을 감싸 다른 write는 진짜 H2로 가고
    // updateOrderIndex만 던지게 한다 → "진짜 쓰인 것들이 롤백되는가"를 검증할 수 있다.
    @MockitoSpyBean
    private WaitingRepository waitingRepository;

    /**
     * 부분 실패의 끝 상태는 셋이다 — [2]슬롯 텅 빔 / [3]예약·대기 이중 존재 / [4]순번 구멍. 하지만 셋 다 같은 @Transactional 경계 하나가 막는 같은 회귀라, 변경이 가장 많이
     * 쌓인 [4](재정렬)를 대표로 검증한다. 지점별 예외 처리(예: 예약금 승격 실패)가 생겨 [2]가 [4]와 다른 회귀가 되면 그때 분리한다.
     */
    @Test
    @DisplayName("[대표 검증·4단계 재정렬] 마지막 단계가 실패하면 취소·승격·대기삭제가 모두 롤백된다")
    void 자동승격_여러단계변경_중간실패_전체롤백() {
        // given: 예약 브라운 + 대기 [콘:1, 모카:2, 핀:3]
        LocalDate date = LocalDate.of(2050, 12, 31);
        Long timeId = fixture.insertTime(LocalTime.of(10, 0));
        Long themeId = fixture.insertTheme("테마A");
        Long brownId = fixture.insertReservation("브라운", date, timeId, themeId);
        Long conId = fixture.insertWaiting("콘", date, timeId, themeId, 1);
        Long mochaId = fixture.insertWaiting("모카", date, timeId, themeId, 2);
        Long pinId = fixture.insertWaiting("핀", date, timeId, themeId, 3);

        // [4] 재정렬 UPDATE에 결함 주입 — 쌓인 변경이 가장 많은 지점 = 대표 케이스
        willThrow(new RuntimeException("재정렬 실패 주입"))
                .given(waitingRepository).updateOrderIndex(anyLong(), anyInt());

        // when: 브라운이 본인 예약 취소 → 자동 승격 → [4]에서 실패
        assertThatThrownBy(() -> reservationService.deleteByOwner(brownId, "브라운"))
                .isInstanceOf(RuntimeException.class);

        // then: 전부 롤백
        assertThat(fixture.findReservationOwner(date, timeId, themeId)).isEqualTo("브라운"); // 취소 안 됨
        assertThat(fixture.existsWaiting(conId)).isTrue();                                  // 콘 승격 안 됨
        assertThat(fixture.findWaitingOrder(conId)).isEqualTo(1);                            // 순번 그대로
        assertThat(fixture.findWaitingOrder(mochaId)).isEqualTo(2);
        assertThat(fixture.findWaitingOrder(pinId)).isEqualTo(3);
    }
}
