package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.support.Fixtures.theme;
import static roomescape.support.Fixtures.time;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.InvalidDomainException;

/**
 * Waiting 도메인 단위 테스트.
 *
 * <p>보호 대상: 대기 객체의 정합성과 불변성, 그리고 자기 정보 비교(같은 슬롯/소유자) 규칙.
 * 외부 의존 없이 입력만으로 결정되므로 가장 빠른 단위 테스트로 검증한다.
 */
class WaitingTest {

    private static final LocalDate DATE = LocalDate.of(2050, 12, 31);

    @Nested
    @DisplayName("순번 제약")
    class OrderValidation {

        @Test
        @DisplayName("순번이 1 미만이면 예외")
        void 순번_0_거부() {
            assertThatThrownBy(() -> Waiting.create("브라운", DATE, time(1), theme(1), 0))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("대기 순번은 1 이상이어야 합니다.");
        }

        @Test
        @DisplayName("순번 1은 허용 (경계값)")
        void 순번_1_허용() {
            assertThatCode(() -> Waiting.create("브라운", DATE, time(1), theme(1), 1))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("이름·필수값 제약")
    class FieldValidation {

        @Test
        @DisplayName("이름이 비어 있으면 예외")
        void 이름_공백() {
            assertThatThrownBy(() -> Waiting.create(" ", DATE, time(1), theme(1), 1))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("대기자 이름은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("날짜가 null이면 예외")
        void 날짜_null() {
            assertThatThrownBy(() -> Waiting.create("브라운", null, time(1), theme(1), 1))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("대기 날짜는 비어 있을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("정보 비교")
    class Comparison {

        @Test
        @DisplayName("같은 슬롯(날짜+시간+테마)인지 비교한다")
        void 슬롯_비교() {
            Waiting w = Waiting.create("브라운", DATE, time(1), theme(1), 1);

            assertThat(w.isSameSlot(DATE, 1L, 1L)).isTrue();
            assertThat(w.isSameSlot(DATE, 1L, 2L)).isFalse();
            assertThat(w.isSameSlot(DATE.plusDays(1), 1L, 1L)).isFalse();
        }

        @Test
        @DisplayName("소유자를 비교한다")
        void 소유자_비교() {
            Waiting w = Waiting.create("브라운", DATE, time(1), theme(1), 1);

            assertThat(w.isOwnedBy("브라운")).isTrue();
            assertThat(w.isOwnedBy("모카")).isFalse();
        }
    }

    @Nested
    @DisplayName("불변성")
    class Immutability {

        @Test
        @DisplayName("withOrderIndex는 원본을 두고 새 순번의 새 객체를 반환한다")
        void 순번_변경_불변() {
            Waiting origin = Waiting.withId(100L, "브라운", DATE, time(1), theme(1), 3);

            Waiting reordered = origin.withOrderIndex(2);

            assertThat(origin.getOrderIndex()).isEqualTo(3);     // 원본 불변
            assertThat(reordered.getOrderIndex()).isEqualTo(2);  // 새 객체에 반영
            assertThat(reordered.getId()).isEqualTo(100L);       // 다른 필드 보존
            assertThat(reordered.getName()).isEqualTo("브라운");
        }
    }
}
