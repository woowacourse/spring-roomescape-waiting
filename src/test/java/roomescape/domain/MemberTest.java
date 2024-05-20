package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;
import roomescape.exception.clienterror.InvalidClientFieldWithValueException;

class MemberTest {

    @DisplayName("이메일, 이름이 공백이면 예외를 발생시킨다.")
    @CsvSource({"email@email.com,", ",test@test.com"})
    @ParameterizedTest
    void given_emailWithName_when_newWithEmptyValue_then_thrownException(String email, String name) {
        assertThatThrownBy(() -> new Member(email, new Password("password", "salt"), name, Role.USER))
                .isInstanceOf(EmptyValueNotAllowedException.class);
    }

    @DisplayName("이메일 양식이 부적절하면 예외를 발생시킨다")
    @Test
    void given_when_newWithInvalidEmailForm_then_thrownException() {
        //given, when, then
        assertThatThrownBy(() -> new Member("poke", new Password("password", "salt"), "poke", Role.ADMIN))
                .isInstanceOf(InvalidClientFieldWithValueException.class);
    }
}
