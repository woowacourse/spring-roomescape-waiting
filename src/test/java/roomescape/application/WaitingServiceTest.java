package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.command.WaitingCreateCommand;
import roomescape.application.dto.result.WaitingResult;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.exception.client.ResourceNotFoundException;
import roomescape.support.ServiceIntegrationTest;

/**
 * WaitingService 통합 테스트 — create/cancel 분기의 메인 검증.
 *
 * <p>WaitingService.create()와 cancelByOwner()의 분기들(예약 없는 슬롯 거부 / 본인 예약 슬롯 거부 /
 * 중복 대기 거부 / 시간·테마 부재 / 대기 취소·재정렬)을 Mock 없이 실제 H2 통합으로 검증한다. 이 분기들은 "이미 저장된 예약/대기" 상태에 의존하는 비즈니스 규칙이라, 실제 DB로 검증하면 분기
 * 로직과 그 상태 의존이 한 흐름에서 함께 동작함을 보장받는다. (모드 A: 분기가 서비스 안의 if-throw 로직이고 분기마다 다른 사용자 경험을 주므로 분기 전부를 본다.)
 *
 * <p>이 자리가 "메인"인 이유(마찰 2의 (B) 결론): 같은 분기를 Mock 단위로 짠 WaitingServiceMockTest와
 * 직접 비교한 결과, 분기 5개는 아직 셋업이 폭발하지 않고 fixture가 한 줄 셋업이라 통합 준비 비용이 작으며, 다른 세 서비스가 모두 통합이라 일관성 가치가 크다. 그래서 통합을 메인으로 둔다. Mock
 * 비교본은 학습 기록으로 park했다(@Disabled). 자세한 4축 판단 근거는 그 파일 주석에 있다. (사이클 2에서 트랜잭션이 들어와 "결과로 안 보이는 협력"이 생기면 Mock 필요성을 다시 본다.)
 *
 * <p>관찰 포인트(학습용): create 한 분기를 검증하려면 매번 시간·테마·예약을 깔아야 한다. 이 "준비
 * 비용"이 분기 수만큼 반복되는 게 통합의 특징이고, 분기가 폭발하면 그때 Mock의 셋업 이점이 커진다.
 */
class WaitingServiceTest extends ServiceIntegrationTest {

    private static final LocalDate FUTURE_DATE = LocalDate.of(2050, 12, 31);

    @Autowired
    private WaitingService waitingService;

    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUpSlot() {
        timeId = fixture.insertTime(LocalTime.of(10, 0));
        themeId = fixture.insertTheme("테마A");
    }

    @Nested
    @DisplayName("대기 신청")
    class Create {

        @Test
        @DisplayName("예약된 슬롯에 첫 대기를 신청하면 순번 1로 생성된다")
        void 첫_대기() {
            // given: 슬롯에 예약이 이미 1건 있어야 대기가 가능하다
            fixture.insertReservation("브라운", FUTURE_DATE, timeId, themeId);

            WaitingResult result = waitingService.create(
                    new WaitingCreateCommand("콘", FUTURE_DATE, timeId, themeId));

            assertThat(result.getName()).isEqualTo("콘");
            assertThat(result.getOrderIndex()).isEqualTo(1);
        }

        @Test
        @DisplayName("이미 대기가 1건 있으면 다음 대기는 순번 2를 받는다")
        void 두번째_대기() {
            fixture.insertReservation("브라운", FUTURE_DATE, timeId, themeId);
            fixture.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);

            WaitingResult result = waitingService.create(
                    new WaitingCreateCommand("모카", FUTURE_DATE, timeId, themeId));

            assertThat(result.getOrderIndex()).isEqualTo(2);
        }

        @Test
        @DisplayName("[분기] 예약이 없는 슬롯에 대기 신청 → 거부 (대기가 아니라 예약을 해야 함)")
        void 예약_없는_슬롯_거부() {
            // given: 예약을 깔지 않는다 → 빈 슬롯
            assertThatThrownBy(() -> waitingService.create(
                    new WaitingCreateCommand("콘", FUTURE_DATE, timeId, themeId)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("예약 가능한 시간입니다. 대기가 아닌 예약을 신청해 주세요.");
        }

        @Test
        @DisplayName("[분기] 이미 본인이 예약한 슬롯에 대기 신청 → 거부")
        void 본인_예약_슬롯_거부() {
            // given: 콘이 이미 그 슬롯의 예약자다
            fixture.insertReservation("콘", FUTURE_DATE, timeId, themeId);

            assertThatThrownBy(() -> waitingService.create(
                    new WaitingCreateCommand("콘", FUTURE_DATE, timeId, themeId)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("이미 본인이 예약한 시간에는 대기를 신청할 수 없습니다.");
        }

        @Test
        @DisplayName("[분기] 같은 사용자가 같은 슬롯에 중복 대기 → 거부")
        void 중복_대기_거부() {
            fixture.insertReservation("브라운", FUTURE_DATE, timeId, themeId);
            fixture.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);

            assertThatThrownBy(() -> waitingService.create(
                    new WaitingCreateCommand("콘", FUTURE_DATE, timeId, themeId)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("이미 해당 시간에 대기 신청한 내역이 있습니다.");
        }

        @Test
        @DisplayName("[분기] 존재하지 않는 시간 ID → 404성 예외")
        void 존재하지_않는_시간() {
            assertThatThrownBy(() -> waitingService.create(
                    new WaitingCreateCommand("콘", FUTURE_DATE, 9999L, themeId)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("존재하지 않는 시간입니다.");
        }

        @Test
        @DisplayName("[분기] 존재하지 않는 테마 ID → 404성 예외 (시간 조회와 별개 경로)")
        void 존재하지_않는_테마() {
            assertThatThrownBy(() -> waitingService.create(
                    new WaitingCreateCommand("콘", FUTURE_DATE, timeId, 9999L)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("존재하지 않는 테마입니다.");
        }
    }

    @Nested
    @DisplayName("대기 취소")
    class Cancel {

        @Test
        @DisplayName("본인 대기를 취소할 수 있다")
        void 본인_취소() {
            fixture.insertReservation("브라운", FUTURE_DATE, timeId, themeId);
            Long waitingId = fixture.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);

            waitingService.cancelByOwner(waitingId, "콘");

            assertThat(fixture.existsWaiting(waitingId)).isFalse();
        }

        @Test
        @DisplayName("[분기] 남의 대기를 취소하려 하면 거부 (존재하지 않는 것으로 취급)")
        void 타인_취소_거부() {
            fixture.insertReservation("브라운", FUTURE_DATE, timeId, themeId);
            Long waitingId = fixture.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);

            assertThatThrownBy(() -> waitingService.cancelByOwner(waitingId, "모카"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("존재하지 않는 대기입니다.");
        }

        @Test
        @DisplayName("중간 대기를 취소하면 뒤 대기의 순번이 당겨진다 (재정렬이 DB에 반영되는지)")
        void 중간_취소_순번_재정렬() {
            fixture.insertReservation("브라운", FUTURE_DATE, timeId, themeId);
            Long w1 = fixture.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);
            Long w2 = fixture.insertWaiting("모카", FUTURE_DATE, timeId, themeId, 2);
            Long w3 = fixture.insertWaiting("핀", FUTURE_DATE, timeId, themeId, 3);

            waitingService.cancelByOwner(w2, "모카");

            // 재정렬 "규칙"은 WaitingsTest가 검증했다. 여기서는 그 규칙이
            // updateOrderIndex를 통해 실제 DB에 반영되는 결합부만 확인한다.
            assertThat(fixture.findWaitingOrder(w1)).isEqualTo(1);  // 앞은 유지
            assertThat(fixture.findWaitingOrder(w3)).isEqualTo(2);  // 뒤는 당겨짐
        }
    }
}
