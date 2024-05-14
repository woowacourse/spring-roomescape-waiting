package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.InvalidClientFieldWithValueException;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

class MemberTest {

    @DisplayName("이메일, 이름 또는 역할 공백이면 예외를 발생시킨다.")
    @CsvSource({"email@email.com,,role", ",test@test.com,role", "email@email.com,test,"})
    @ParameterizedTest
    void given_emailWithName_when_newWithEmptyValue_then_thrownException(String email, String name, String role) {
        assertThatThrownBy(() -> new Member(email, new Password("password", "salt"), name, role)).isInstanceOf(
                EmptyValueNotAllowedException.class);
    }

    @DisplayName("관리자 권한이 존재하는지 확인할 수 있다.")
    @Test
    void given_memberWithAdminRole_when_isAdmin_then_true() {
        //given
        final Member member = new Member("poke@test.com", new Password("password", "salt"), "poke", "ADMIN");
        //when,then
        assertThat(member.isAdmin()).isTrue();
    }

    @DisplayName("이메일 양식이 부적절하면 예외를 발생시킨다")
    @Test
    void given_when_newWithInvalidEmailForm_then_thrownException() {
        //given, when, then
        assertThatThrownBy(() -> new Member("poke", new Password("password", "salt"), "poke", "ADMIN"))
                .isInstanceOf(InvalidClientFieldWithValueException.class);
    }
}
