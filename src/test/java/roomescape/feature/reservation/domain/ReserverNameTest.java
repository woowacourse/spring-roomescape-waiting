package roomescape.feature.reservation.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.global.error.exception.GeneralException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReserverNameTest {

    private static final String VALID_NAME = "홍길동";
    private static final String MAX_LENGTH_NAME = "a".repeat(20);
    private static final String EXCEEDED_LENGTH_NAME = "a".repeat(21);

    @Nested
    class 생성_시점에_값을_검증한다 {

        @Test
        void 유효한_이름이라면_생성에_성공한다() {
            // when
            ReserverName reserverName = new ReserverName(VALID_NAME);

            // then
            assertThat(reserverName.value()).isEqualTo(VALID_NAME);
        }

        @Test
        void 이름이_정확히_20자라면_생성에_성공한다() {
            // when
            ReserverName reserverName = new ReserverName(MAX_LENGTH_NAME);

            // then
            assertThat(reserverName.value()).isEqualTo(MAX_LENGTH_NAME);
        }

        @Test
        void 이름이_20자를_초과한다면_예외를_던진다() {
            assertThatThrownBy(() -> new ReserverName(EXCEEDED_LENGTH_NAME))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("상태 값이 올바르지 않습니다.");
        }

        @Test
        void 이름이_비어있다면_예외를_던진다() {
            assertThatThrownBy(() -> new ReserverName(""))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("상태 값이 올바르지 않습니다.");
        }

        @Test
        void 이름이_공백으로만_이루어졌다면_예외를_던진다() {
            assertThatThrownBy(() -> new ReserverName("   "))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("상태 값이 올바르지 않습니다.");
        }

        @Test
        void 이름이_null이라면_예외를_던진다() {
            assertThatThrownBy(() -> new ReserverName(null))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("상태 값이 올바르지 않습니다.");
        }
    }
}
