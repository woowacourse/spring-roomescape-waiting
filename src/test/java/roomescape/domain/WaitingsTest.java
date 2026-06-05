package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.client.BusinessRuleViolationException;

class WaitingsTest {

    private static final LocalDate DATE = LocalDate.of(2050, 12, 31);
    private static final ReservationTime TIME =
            ReservationTime.withId(1L, LocalTime.of(10, 0));
    private static final Theme THEME =
            Theme.withId(1L, "테마A", "설명", "url");

    private Waiting waiting(Long id, String name, int order) {
        return Waiting.withId(id, name, DATE, TIME, THEME, order);
    }

    @Nested
    @DisplayName("nextOrder")
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
        @DisplayName("같은 이름이 있으면 거부")
        void 중복_거부() {
            Waitings ws = new Waitings(List.of(waiting(1L, "브라운", 1)));
            assertThatThrownBy(() ->
                    ws.validateNoDuplicateBy("브라운"))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("이미 해당 시간에 대기 신청한 내역이 있습니다.");
        }

        @Test
        @DisplayName("다른 이름이면 허용")
        void 다른_이름_허용() {
            Waitings ws = new Waitings(List.of(waiting(1L, "브라운", 1)));
            assertThatCode(() ->
                    ws.validateNoDuplicateBy("모카"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("firstWaiting")
    class First {

        @Test
        @DisplayName("가장 작은 순번의 대기를 반환")
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
    @DisplayName("순번 재정렬")
    class Reorder {

        @Test
        @DisplayName("2번이 빠지면 3,4번이 2,3번이 된다")
        void 중간_제거() {
            Waitings ws = new Waitings(List.of(
                    waiting(1L, "콘", 1),
                    waiting(2L, "모카", 2),
                    waiting(3L, "핀", 3),
                    waiting(4L, "라이언", 4)
            ));

            List<Waiting> reordered = ws.reorderAfterRemoval(2);

            assertThat(reordered).hasSize(2);
            assertThat(reordered.get(0).getName()).isEqualTo("핀");
            assertThat(reordered.get(0).getOrderIndex()).isEqualTo(2);
            assertThat(reordered.get(1).getName()).isEqualTo("라이언");
            assertThat(reordered.get(1).getOrderIndex()).isEqualTo(3);
        }

        @Test
        @DisplayName("1번이 빠지면 2,3,4번이 1,2,3번이 된다 (자동 승격 시나리오)")
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
        @DisplayName("마지막이 빠지면 변경 없음")
        void 마지막_제거() {
            Waitings ws = new Waitings(List.of(
                    waiting(1L, "콘", 1),
                    waiting(2L, "모카", 2)
            ));
            assertThat(ws.reorderAfterRemoval(2)).isEmpty();
        }
    }
}
