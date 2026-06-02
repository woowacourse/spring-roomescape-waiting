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
 * 목적은 "이 분기들에 Mock 단위 테스트가 필요한가"를 두 방식의 코드를 직접 비교해 판단하는 것이다.
 * git diff WaitingServiceTest WaitingServiceMockTest 로 나란히 놓고 보면 차이가 드러난다.
 *
 * <p>=== 통합 흡수본과 비교했을 때 관찰 포인트 ===
 *
 * <p><b>1. 빨라진다.</b> 스프링 컨텍스트도 H2도 안 뜬다. new WaitingService(...)로 직접 만든다.
 * 분기 하나를 검증하는 데 시간·테마·예약을 DB에 까는 준비가 사라지고, given(...).willReturn(...)
 * 한 줄로 "이 상태였다 치고"가 끝난다.
 *
 * <p><b>2. 위험이 생긴다.</b> existsByDateAndTimeAndTheme(...)가 true/false를 "리턴한다 치고" 검증하는데,
 * 그 SQL이 진짜로 그렇게 동작한다는 보장은 이 테스트 어디에도 없다. Mock이 거짓말하면 이 검증은
 * 통째로 무의미해진다. (그래서 토론 규칙 3: Mock으로 Service를 검증하면 Repository 자체는
 * 별도 통합/슬라이스 테스트로 증명해야 한다 → JdbcWaitingRepositoryTest가 그 역할.)
 *
 * <p><b>3. 무엇을 검증하지 "않는가"가 핵심이다.</b> 아래 테스트들은 의도적으로 verify(...)로
 * 호출 순서를 확인하지 않는다. assertThatThrownBy로 "입력(상태) → 올바른 예외"만 본다.
 * verify로 "existsBy가 정확히 1번 불렸는지"까지 박으면, 구현을 조금만 바꿔도 깨지는
 * 리팩터링 내성 약한 테스트가 된다(토론에서 경계한 바로 그 형태).
 * 즉 같은 Mock 테스트라도 "분기 로직의 단위 검증"으로 쓰면 살고, "호출 명세 검증"으로 쓰면 죽는다.
 *
 * <p><b>결론 도출은 학습자의 몫:</b> 이 두 파일을 비교한 뒤
 * "이 분기들은 통합으로 충분한가, Mock 단위가 값을 더하는가"를 스스로 정한다.
 * (예: 분기가 더 많아지고 조합이 폭발하면 Mock 단위의 속도 이점이 커지고,
 *  분기가 적고 상태 의존이 강하면 통합 하나로 충분할 수 있다.)
 */
class WaitingServiceMockTest {

    private static final LocalDate FUTURE = LocalDate.of(2050, 12, 31);

    private final WaitingRepository waitingRepository = mock(WaitingRepository.class);
    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
    private final ThemeRepository themeRepository = mock(ThemeRepository.class);

    private final WaitingService waitingService = new WaitingService(
            waitingRepository, reservationRepository, reservationTimeRepository, themeRepository);

    /**
     * 모든 분기 테스트의 공통 전제: 시간·테마는 존재한다고 가정한다.
     * (존재하지 않는 경우는 별도 테스트에서 다룬다)
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
