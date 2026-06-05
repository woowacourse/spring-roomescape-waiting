package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static roomescape.support.Fixtures.theme;
import static roomescape.support.Fixtures.time;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.application.dto.command.WaitingCreateCommand;
import roomescape.domain.Waiting;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.exception.client.ResourceNotFoundException;

/**
 * WaitingService Mock 단위 테스트 — "비교본".
 *
 * <p>이 파일은 WaitingServiceTest(통합 흡수본)와 <b>같은 분기</b>를 Mockito로 검증한다.
 * 목적은 "이 분기들에 Mock 단위 테스트가 필요한가"를 두 방식의 코드를 직접 비교해 판단하는 것이다. git diff WaitingServiceTest WaitingServiceMockTest 로 나란히
 * 놓고 보면 차이가 드러난다.
 *
 * <p>=== 통합 흡수본과 비교했을 때 관찰 포인트 ===
 *
 * <p><b>1. 빨라진다.</b> 스프링 컨텍스트도 H2도 안 뜬다. new WaitingService(...)로 직접 만든다.
 * 분기 하나를 검증하는 데 시간·테마·예약을 DB에 까는 준비가 사라지고, given(...).willReturn(...) 한 줄로 "이 상태였다 치고"가 끝난다.
 *
 * <p><b>2. 위험이 생긴다.</b> existsByDateAndTimeAndTheme(...)가 true/false를 "리턴한다 치고" 검증하는데,
 * 그 SQL이 진짜로 그렇게 동작한다는 보장은 이 테스트 어디에도 없다. Mock이 거짓말하면 이 검증은 통째로 무의미해진다. (그래서 토론 규칙 3: Mock으로 Service를 검증하면 Repository
 * 자체는 별도 통합/슬라이스 테스트로 증명해야 한다 → JdbcWaitingRepositoryTest가 그 역할.)
 *
 * <p><b>3. 무엇을 검증하지 "않는가"가 핵심이다.</b> 아래 테스트들은 의도적으로 verify(...)로
 * 호출 순서를 확인하지 않는다. assertThatThrownBy로 "입력(상태) → 올바른 예외"만 본다. verify로 "existsBy가 정확히 1번 불렸는지"까지 박으면, 구현을 조금만 바꿔도 깨지는
 * 리팩터링 내성 약한 테스트가 된다(토론에서 경계한 바로 그 형태). 즉 같은 Mock 테스트라도 "분기 로직의 단위 검증"으로 쓰면 살고, "호출 명세 검증"으로 쓰면 죽는다.
 *
 * <p>=== 결론 도출: 네 축으로 판단했다 ===
 *
 * <p><b>축 1 — 셋업 비용:</b> 통합은 분기마다 시간·테마·예약/대기를 DB에 깐다(분기 5개면 5번).
 * Mock은 given(...).willReturn(...) 한 줄. 분기·상태 조합이 폭발하면 Mock이 압도적으로 가볍고, 분기가 적고 셋업이 단순하면 통합도 부담이 작다.
 *
 * <p><b>축 2 — 실행 비용:</b> 통합은 풀 컨텍스트 + H2(수백 ms), Mock은 컨텍스트 없음(수십 ms).
 * CI 누적이 큰 규모부터 체감되며, 사이클 1 규모에선 결정적 단점은 아니다.
 *
 * <p><b>축 3 — 회귀 안전성의 종류:</b> 통합은 분기 로직 + SQL 정확성 + DTO 매핑 + 빈 협력을 한 번에
 * 보장한다. Mock은 분기 로직만 보장하고 SQL은 "리턴한다 치고"라 보장하지 않는다. Mock으로 가려면 Repository SQL을 슬라이스(JdbcWaitingRepositoryTest)로 반드시
 * 짝지어야 한다.
 *
 * <p><b>축 4 — 핀포인트 vs 리팩토링 친화성:</b> 통합은 결과 검증이라 깨졌을 때 원인 추적이 약하지만
 * 구현을 바꿔도 결과만 같으면 통과한다. Mock은 분기 핀포인트가 강하지만 verify로 호출 명세를 박으면 리팩토링에 취약하다. 이 파일은 verify를 쓰지 않아 그 취약성을 피했다.
 *
 * <p><b>판단 가이드:</b> 분기 많고 조합 폭발 → Mock 가치↑ / 분기 적고 상태 의존 강함 → 통합으로 충분 /
 * SQL 회귀가 결정적 → 통합 우선 또는 슬라이스 짝짓기 / 여러 도메인이 흐름으로 협력 → 통합 우선 / 외부 시스템 호출·격리 어려운 의존 → Mock 필수.
 *
 * <p>=== WaitingService.create에 적용한 결론 (사이클 1: B) ===
 * <ul>
 *   <li>분기 5개 — 중간. 셋업 폭발 "직전"이지만 아직 폭발은 아니다.</li>
 *   <li>fixture가 한 줄 셋업이라 통합 준비 비용이 작다.</li>
 *   <li>다른 세 서비스(Reservation/ReservationTime/Theme)가 모두 통합 — 여기만 Mock이면 비대칭.</li>
 *   <li>슬라이스가 SQL을 책임져(하지만 100프로 책임진다는 것 조차 아직 확신을 못느껴 찜찜) Mock도 정당화되지만, 위 일관성·낮은 셋업 비용이 더 크다.</li>
 * </ul>
 * → 통합(WaitingServiceTest)을 메인으로 두고, 이 Mock 비교본은 학습 기록으로 park한다.
 *
 * <p>=== 사이클 2 재검토 결과 (park 유지) ===
 *
 * @Transactional이 들어온 뒤 두 기준으로 다시 봤다. (1) 분기/조합 폭발? — create 분기는 5개 그대로다. 사이클 2의 트랜잭션 작업은 cancelByOwner/deleteByOwner에
 * 경계를 준 것이고, create는 단일 INSERT라 손대지 않았다. 분기 상황이 안 변했으니 분기 검증은 여전히 통합(WaitingServiceTest)이 메인이다. (2) 결과로 안 보이는 협력? —
 * 도착했다. 다만 그 협력(트랜잭션 롤백)을 검증한 도구는 이 파일의 "create 분기 순수 mock"이 아니라 spy 결함 주입이다. → ReservationPromotionTransactionTest /
 * WaitingCancelTransactionTest 가 @MockitoSpyBean으로 한 메서드만 던지게 해 롤백을 증명한다. "Mock 가치가 살아나는 자리"라는 사이클 1의 예측은 맞았지만, 그 자리는
 * create 분기가 아니라 cancel/delete의 경계였고 도구도 full mock이 아니라 spy였다.
 * <p>
 * 결론: 승격 사유가 없어 park 유지. 이 비교가 끝내 가른 것은 "Mock의 적정 범위"다 — 결함 주입(경계 증명)엔 적정, 분기 단위 검증엔 이 규모에선 통합이 메인. 다음 재검토 트리거: 알림
 * 전송·외부 시스템 호출 등 "결과로 안 보이는 협력"이 create/promote 흐름 자체에 들어올 때(통합으로 흉내 내기 어려운 의존이 생겨 Mock 필요성이 올라가는 시점).
 * <p>
 * <p>
 * 트랜잭션 경계는 "결과로 안 보이는 협력"의 전형이라 Mock 가치가 결정적으로 살아날 수 있는 자리다.
 * @Transactional을 넣으며 "분기/조합이 폭발하는가, 결과로 안 보이는 협력이 생기는가"를 다시 보고, 그때 이 park를 풀지(= Mock 단위를 메인 검증의 하나로 승격할지) 다시 결정한다.
 */
@Disabled("Mock vs 통합 학습 비교 + 사이클 2 재검토 완료(park 유지). 활성 검증은 WaitingServiceTest(통합). 다음 트리거=알림/외부 호출 도입 시")
class WaitingServiceMockTest {

    private static final LocalDate FUTURE = LocalDate.of(2050, 12, 31);

    private final WaitingRepository waitingRepository = mock(WaitingRepository.class);
    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
    private final ThemeRepository themeRepository = mock(ThemeRepository.class);

    private final WaitingService waitingService = new WaitingService(
            waitingRepository, reservationRepository, reservationTimeRepository, themeRepository);

    /**
     * 모든 분기 테스트의 공통 전제: 시간·테마는 존재한다고 가정한다. (존재하지 않는 경우는 별도 테스트에서 다룬다)
     */
    private void givenTimeAndThemeExist() {
        given(reservationTimeRepository.findById(anyLong()))
                .willReturn(Optional.of(time(1)));
        given(themeRepository.findById(anyLong()))
                .willReturn(Optional.of(theme(1)));
    }

    @Nested
    @DisplayName("대기 신청 분기")
    class CreateBranches {

        @Test
        @DisplayName("[분기] 예약이 없는 슬롯 → 거부")
        void 예약_없는_슬롯_거부() {
            givenTimeAndThemeExist();
            // "본인 예약 없음" 그리고 "슬롯에 예약 없음"인 상태라고 가정한다
            given(reservationRepository.existsBySlotAndName(any(), anyLong(), anyLong(), any()))
                    .willReturn(false);
            given(reservationRepository.existsByDateAndTimeAndTheme(any(), anyLong(), anyLong()))
                    .willReturn(false);  // ← 예약 없는 슬롯

            assertThatThrownBy(() -> waitingService.create(
                    new WaitingCreateCommand("콘", FUTURE, 1L, 1L)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("예약 가능한 시간입니다. 대기가 아닌 예약을 신청해 주세요.");
        }

        @Test
        @DisplayName("[분기] 이미 본인이 예약한 슬롯 → 거부")
        void 본인_예약_슬롯_거부() {
            givenTimeAndThemeExist();
            given(reservationRepository.existsBySlotAndName(any(), anyLong(), anyLong(), any()))
                    .willReturn(true);  // ← 본인이 이미 예약자

            assertThatThrownBy(() -> waitingService.create(
                    new WaitingCreateCommand("콘", FUTURE, 1L, 1L)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("이미 본인이 예약한 시간에는 대기를 신청할 수 없습니다.");
        }

        @Test
        @DisplayName("[분기] 같은 사용자가 이미 대기 중 → 거부")
        void 중복_대기_거부() {
            givenTimeAndThemeExist();
            given(reservationRepository.existsBySlotAndName(any(), anyLong(), anyLong(), any()))
                    .willReturn(false);
            given(reservationRepository.existsByDateAndTimeAndTheme(any(), anyLong(), anyLong()))
                    .willReturn(true);  // 예약은 있다 (대기 가능 조건)
            // 슬롯에 콘의 대기가 이미 있다고 가정
            given(waitingRepository.findBySlot(any(), anyLong(), anyLong()))
                    .willReturn(List.of(Waiting.withId(1L, "콘", FUTURE, time(1), theme(1), 1)));

            assertThatThrownBy(() -> waitingService.create(
                    new WaitingCreateCommand("콘", FUTURE, 1L, 1L)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("이미 해당 시간에 대기 신청한 내역이 있습니다.");
        }

        @Test
        @DisplayName("[분기] 존재하지 않는 시간 → 404성 예외")
        void 존재하지_않는_시간() {
            given(reservationTimeRepository.findById(anyLong()))
                    .willReturn(Optional.empty());  // ← 시간 없음

            assertThatThrownBy(() -> waitingService.create(
                    new WaitingCreateCommand("콘", FUTURE, 9999L, 1L)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("존재하지 않는 시간입니다.");
        }

        @Test
        @DisplayName("정상: 모든 조건 통과 시 저장된다")
        void 정상_생성() {
            givenTimeAndThemeExist();
            given(reservationRepository.existsBySlotAndName(any(), anyLong(), anyLong(), any()))
                    .willReturn(false);
            given(reservationRepository.existsByDateAndTimeAndTheme(any(), anyLong(), anyLong()))
                    .willReturn(true);
            given(waitingRepository.findBySlot(any(), anyLong(), anyLong()))
                    .willReturn(List.of());  // 기존 대기 없음 → 순번 1
            given(waitingRepository.save(any()))
                    .willReturn(Waiting.withId(10L, "콘", FUTURE, time(1), theme(1), 1));

            assertThatCode(() -> waitingService.create(
                    new WaitingCreateCommand("콘", FUTURE, 1L, 1L)))
                    .doesNotThrowAnyException();
        }
    }
}
