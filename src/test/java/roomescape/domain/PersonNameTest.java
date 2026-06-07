package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

class PersonNameTest {

    @Nested
    class 생성 {

        @Test
        void 성공() {
            String value = "검프";
            PersonName personName = new PersonName(value);
            assertThat(personName.getName()).isEqualTo(value);
        }

        @Test
        void null이면_예외발생() {
            assertThatThrownBy(() -> new PersonName(null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERSON_NAME_NULL_OR_BLANK);
        }

        @Test
        void 빈_문자열이면_예외발생() {
            assertThatThrownBy(() -> new PersonName(""))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERSON_NAME_NULL_OR_BLANK);
        }

        @Test
        void 공백만_있으면_예외발생() {
            assertThatThrownBy(() -> new PersonName("   "))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERSON_NAME_NULL_OR_BLANK);
        }
    }
}
