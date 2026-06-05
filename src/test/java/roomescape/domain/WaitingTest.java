package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.exception.InvalidDomainException;

class WaitingTest {

    private static final LocalDate DATE = LocalDate.of(2050, 12, 31);
    public static final long TIME_ID = 1L;
    private static final ReservationTime TIME = ReservationTime.withId(TIME_ID, LocalTime.of(10, 0));
    public static final long THEME_ID = 1L;
    private static final Theme THEME = Theme.withId(THEME_ID, "테마A", "설명", "url");

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
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessageContaining("대기자 이름은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("이름 30자 초과 시 거부")
        void 이름_30자_초과_거부() {
            String longName = "a".repeat(31);
            assertThatThrownBy(() -> Waiting.create(longName, DATE, TIME, THEME, 1))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessageContaining("대기자 이름은 30자를 초과할 수 없습니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        @DisplayName("순번이 0 이하면 거부")
        void 순번_0이하_거부(int orderIndex) {
            assertThatThrownBy(() -> Waiting.create("브라운", DATE, TIME, THEME, orderIndex))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessageContaining("대기 순번은 1 이상이어야 합니다.");
        }

        @Test
        @DisplayName("필수 값이 null이면 거부")
        void 필수값_null_거부() {
            assertThatThrownBy(() -> Waiting.create("브라운", null, TIME, THEME, 1))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessageContaining("날짜는 비어 있을 수 없습니다.");

            assertThatThrownBy(() -> Waiting.create("브라운", DATE, null, THEME, 1))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessageContaining("시간은 비어 있을 수 없습니다.");

            assertThatThrownBy(() -> Waiting.create("브라운", DATE, TIME, null, 1))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessageContaining("테마는 비어 있을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("동작")
    class Behavior {

        @Test
        @DisplayName("같은 슬롯인지 비교한다")
        void 슬롯_비교() {
            Waiting w = Waiting.create("브라운", DATE, TIME, THEME, 1);
            assertThat(w.isSameSlot(new Slot(DATE, TIME, THEME))).isTrue();

            Theme anotherTheme = Theme.withId(2L, "테마B", "설명", "url");
            assertThat(w.isSameSlot(new Slot(DATE, TIME, anotherTheme))).isFalse();
            assertThat(w.isSameSlot(new Slot(DATE.plusDays(1), TIME, THEME))).isFalse();
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

            assertThat(w.getOrderIndex()).isEqualTo(3);
            assertThat(reordered.getOrderIndex()).isEqualTo(2);
            assertThat(reordered.getId()).isEqualTo(100L);
            assertThat(reordered.getName()).isEqualTo("브라운");
            assertThat(reordered.getDate()).isEqualTo(DATE);
            assertThat(reordered.getTime()).isEqualTo(TIME);
            assertThat(reordered.getTheme()).isEqualTo(THEME);
        }
    }
}
