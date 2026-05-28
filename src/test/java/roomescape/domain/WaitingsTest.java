package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.support.Fixtures.waiting;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.client.BusinessRuleViolationException;

/**
 * Waitings(일급 컬렉션) 단위 테스트.
 *
 * <p>이 테스트가 이 미션 테스트 전략의 핵심이다. 순번 부여·중복 검증·재정렬이라는
 * "비즈니스 규칙"이 외부 의존 없이 이 컬렉션 안에 모여 있으므로, DB 없이 여기서 전부 검증한다.
 *
 * <p>그 결과 통합/인수 테스트는 "순번이 1,2,3으로 맞는가"를 다시 검증할 필요가 없다.
 * E2E는 "이 규칙이 HTTP 흐름에 연결됐는가"만 대표 케이스로 한 번 확인하면 된다.
 * (토론 규칙: 같은 것을 두 번 검증하지 않는다 — 단위가 빠르고 또렷하다.)
 */
class WaitingsTest {

    @Nested
    @DisplayName("다음 순번 계산")
    class NextOrder {

        @Test
        @DisplayName("빈 컬렉션이면 다음 순번은 1")
        void 빈_컬렉션() {
            assertThat(new Waitings(List.of()).nextOrderIndex()).isEqualTo(1);
        }

        @Test
        @DisplayName("3개 있으면 다음 순번은 4")
        void 세개_있을때() {
            Waitings ws = new Waitings(List.of(
                    waiting(1L, "콘", 1),
                    waiting(2L, "모카", 2),
                    waiting(3L, "핀", 3)
            ));
            assertThat(ws.nextOrderIndex()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("중복 대기 검증")
    class Duplicate {

        @Test
        @DisplayName("같은 이름이 이미 있으면 거부")
        void 중복_거부() {
            Waitings ws = new Waitings(List.of(waiting(1L, "브라운", 1)));

            assertThatThrownBy(() -> ws.validateNoDuplicateBy("브라운"))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("이미 해당 시간에 대기 신청한 내역이 있습니다.");
        }

        @Test
        @DisplayName("다른 이름이면 허용")
        void 다른_이름_허용() {
            Waitings ws = new Waitings(List.of(waiting(1L, "브라운", 1)));

            assertThatCode(() -> ws.validateNoDuplicateBy("모카"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("첫 대기 조회")
    class First {

        @Test
        @DisplayName("입력 순서와 무관하게 가장 작은 순번을 반환한다")
        void 가장_앞_대기() {
            Waitings ws = new Waitings(List.of(
                    waiting(3L, "핀", 3),
                    waiting(1L, "콘", 1),
                    waiting(2L, "모카", 2)
            ));

            assertThat(ws.firstWaiting()).isPresent();
            assertThat(ws.firstWaiting().get().getName()).isEqualTo("콘");
        }

        @Test
        @DisplayName("빈 컬렉션이면 Optional.empty")
        void 빈_컬렉션() {
            assertThat(new Waitings(List.of()).firstWaiting()).isEmpty();
        }
    }

    @Nested
    @DisplayName("제거 후 재정렬")
    class Reorder {

        @Test
        @DisplayName("중간(2번)이 빠지면 뒤 순번이 한 칸씩 당겨진다")
        void 중간_제거() {
            Waitings ws = new Waitings(List.of(
                    waiting(1L, "콘", 1),
                    waiting(2L, "모카", 2),
                    waiting(3L, "핀", 3),
                    waiting(4L, "라이언", 4)
            ));

            List<Waiting> reordered = ws.reorderAfterRemoval(2);

            assertThat(reordered).hasSize(2);
            assertThat(reordered).extracting(Waiting::getName)
                    .containsExactly("핀", "라이언");
            assertThat(reordered).extracting(Waiting::getOrderIndex)
                    .containsExactly(2, 3);
        }

        @Test
        @DisplayName("1번이 빠지면 나머지가 1,2,...로 당겨진다 (승격 시나리오)")
        void 첫번째_제거() {
            Waitings ws = new Waitings(List.of(
                    waiting(1L, "콘", 1),
                    waiting(2L, "모카", 2),
                    waiting(3L, "핀", 3)
            ));

            List<Waiting> reordered = ws.reorderAfterRemoval(1);

            assertThat(reordered).extracting(Waiting::getOrderIndex)
                    .containsExactly(1, 2);
        }

        @Test
        @DisplayName("마지막이 빠지면 당겨질 대기가 없다")
        void 마지막_제거() {
            Waitings ws = new Waitings(List.of(
                    waiting(1L, "콘", 1),
                    waiting(2L, "모카", 2)
            ));

            assertThat(ws.reorderAfterRemoval(2)).isEmpty();
        }
    }
}
