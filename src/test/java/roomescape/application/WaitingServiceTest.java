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
 * WaitingService 통합 테스트 — "분기 흡수본".
 *
 * <p>이 파일은 마찰 2의 (B) 선택을 구현한다: WaitingService.create()의 분기들
 * (이미 본인이 예약한 슬롯 거부 / 예약 없는 슬롯 거부 / 중복 대기 거부)을
 * <b>Mock 없이 실제 H2 통합 테스트로</b> 검증한다.
 *
 * <p>커밋 9의 WaitingServiceMockTest(같은 분기를 Mock 단위로)와 대조하기 위한 기준선이다.
 * 이 파일을 먼저 작성해 "통합으로 흡수했을 때 무엇이 들고, 무엇이 무거운가"를 체감한 뒤, Mock 버전과 diff로 비교하며 "Mock 서비스 단위 테스트가 정말 필요한가"를 판단한다.
 *
 * <p>검증 시선: 이 분기들은 "시스템 상태(이미 저장된 예약/대기)에 의존하는 비즈니스 규칙"이다.
 * Mock으로 existsBy...의 결과를 흉내 내면 "그 SQL이 진짜 그렇게 동작한다"는 보장이 없다. 실제 DB로 검증하면 그 보장까지 함께 얻는다. (토론 규칙 3)
 *
 * <p>관찰 포인트(학습용): create의 한 분기를 검증하려면 given에서 매번
 * 시간·테마·예약을 깔아야 한다. 이 "준비 비용"이 분기 수만큼 반복되는 게 통합 흡수본의 특징이다.
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
