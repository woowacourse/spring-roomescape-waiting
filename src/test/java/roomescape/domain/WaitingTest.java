package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.InvalidDomainException;

class WaitingTest {

    private static final LocalDate DATE = LocalDate.of(2050, 12, 31);
    private static final ReservationTime TIME =
            ReservationTime.withId(1L, LocalTime.of(10, 0));
    private static final Theme THEME =
            Theme.withId(1L, "테마A", "설명", "url");

    @Nested
    @DisplayName("Waiting 객체 생성 시 도메인 제약 검증")
    class Creation {

        @Test
        @DisplayName("정상 케이스: 모든 필드가 유효하면 생성된다")
        void 정상_생성() {
            Waiting w = Waiting.create("브라운", DATE, TIME, THEME, 1);
            assertThat(w.getName()).isEqualTo("브라운");
            assertThat(w.getOrderIndex()).isEqualTo(1);
        }

        @Test
        @DisplayName("이름이 비어있으면 거부")
        void 빈_이름_거부() {
            assertThatThrownBy(() -> Waiting.create("", DATE, TIME, THEME, 1))
                    .isInstanceOf(InvalidDomainException.class);
        }

        @Test
        @DisplayName("이름 30자 초과 시 거부")
        void 이름_30자_초과_거부() {
            String longName = "a".repeat(31);
            assertThatThrownBy(() -> Waiting.create(longName, DATE, TIME, THEME, 1))
                    .isInstanceOf(InvalidDomainException.class);
        }

        @Test
        @DisplayName("순번이 0 이하면 거부")
        void 순번_0이하_거부() {
            assertThatThrownBy(() -> Waiting.create("브라운", DATE, TIME, THEME, 0))
                    .isInstanceOf(InvalidDomainException.class);
            assertThatThrownBy(() -> Waiting.create("브라운", DATE, TIME, THEME, -1))
                    .isInstanceOf(InvalidDomainException.class);
        }

        @Test
        @DisplayName("필수 값이 null이면 거부")
        void 필수값_null_거부() {
            assertThatThrownBy(() -> Waiting.create("브라운", null, TIME, THEME, 1))
                    .isInstanceOf(InvalidDomainException.class);

            assertThatThrownBy(() -> Waiting.create("브라운", DATE, null, THEME, 1))
                    .isInstanceOf(InvalidDomainException.class);

            assertThatThrownBy(() -> Waiting.create("브라운", DATE, TIME, null, 1))
                    .isInstanceOf(InvalidDomainException.class);
        }

    }

    @Nested
    @DisplayName("동작")
    class Behavior {

        //TODO Slot 값 객체로 분리할 신호
        // 1. 도메인 언어가 생김 -> 파라미터 실수 컴파이단에서 발 + 차후 미래 규칙관련 모을수잇음
        @Test
        @DisplayName("같은 슬롯인지 비교한다")
        void 슬롯_비교() {
            Waiting w = Waiting.create("브라운", DATE, TIME, THEME, 1);
            assertThat(w.isSameSlot(DATE, 1L, 1L)).isTrue();
            assertThat(w.isSameSlot(DATE, 1L, 2L)).isFalse();
            assertThat(w.isSameSlot(DATE.plusDays(1), 1L, 1L)).isFalse();
        }

        @Test
        @DisplayName("소유자를 비교한다")
        void 소유자_비교() {
            Waiting w = Waiting.create("브라운", DATE, TIME, THEME, 1);
            assertThat(w.isOwnedBy("브라운")).isTrue();
            assertThat(w.isOwnedBy("모카")).isFalse();
        }

        @Test
        @DisplayName("withOrder는 새 순번을 가진 새 객체를 반환한다 (불변성)")
        void 순번_변경() {
            Waiting w = Waiting.withId(100L, "브라운", DATE, TIME, THEME, 3);
            Waiting reordered = w.withOrderIndex(2);

            assertThat(w.getOrderIndex()).isEqualTo(3);           // 원본 불변
            assertThat(reordered.getOrderIndex()).isEqualTo(2);
            assertThat(reordered.getId()).isEqualTo(100L);   // 다른 필드 보존
        }
    }
}
