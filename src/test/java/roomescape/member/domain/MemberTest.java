package roomescape.member.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.global.exception.model.ValidateException;

@DataJpaTest
class MemberTest {

    @PersistenceContext
    private EntityManager em;

    @ParameterizedTest
    @ValueSource(strings = {"", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("회원의 이름이 1자 이상 50자 이하가 아니라면 예외를 발생시킨다.")
    void validateMemberNameLength(String name) {
        Assertions.assertThatThrownBy(() -> new Member(name, "abc@aaa.com", "12341234"))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    @DisplayName("회원의 이름이 공백이면 예외를 발생시킨다.")
    void validateMemberNameBlank() {
        String name = " ";

        Assertions.assertThatThrownBy(() -> new Member(name, "abc@aaa.com", "12341234"))
                .isInstanceOf(ValidateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234124", "abcdwfawf", ""})
    @DisplayName("회원의 이메일이 형식이 맞지 않으면 예외를 발생시킨다.")
    void validateMemberEmailFormat(String email) {
        Assertions.assertThatThrownBy(() -> new Member("name", email, "12341234"))
                .isInstanceOf(ValidateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdefg", "abcdefghijklmnopv"})
    @DisplayName("회원의 패스워드가 8~16자 이내가 아니라면 예외를 발생시킨다.")
    void validateMemberPasswordLength(String password) {
        Assertions.assertThatThrownBy(() -> new Member("name", "abc@aaa.com", password))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    @DisplayName("회원의 패스워드가 공백이면 예외를 발생시킨다.")
    void validateMemberPasswordBlank() {
        String password = "          ";

        Assertions.assertThatThrownBy(() -> new Member("name", "abc@aaa.com", password))
                .isInstanceOf(ValidateException.class);
    }
}
