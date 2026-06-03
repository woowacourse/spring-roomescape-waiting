package roomescape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonNameTest {

    @Test
    void PersonName_객체_생성() {
        final String name = "검프";

        final PersonName personName = new PersonName(name);

        assertThat(personName.name()).isEqualTo(name);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 이름이_null이거나_비어있으면_예외발생(final String name) {
        assertThatThrownBy(() -> new PersonName(name))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERSON_NAME_NULL_OR_BLANK);
    }

    @Test
    void 문자열과_이름이_같은지_확인() {
        final String sameName = "검프";
        final String differentName = "류시";
        final PersonName personName = new PersonName(sameName);

        assertThat(personName.isSameName(sameName)).isTrue();
        assertThat(personName.isSameName(differentName)).isFalse();
    }
}
