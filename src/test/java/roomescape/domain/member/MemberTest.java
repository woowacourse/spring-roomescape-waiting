package roomescape.domain.member;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class MemberTest {

    static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of(" "),
                Arguments.of(""),
                Arguments.of((String) null)
        );
    }

    @DisplayName("공백이거나 이름이 존재하지 않는 경우 생성할 수 없다.")
    @ParameterizedTest
    @MethodSource("invalidNames")
    void invalidReservationNameTest(String reservationName) {
        Assertions.assertThatThrownBy(() -> new Member(1L, reservationName, "hello@woowa.com", Role.USER, "password"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("email 이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidEmailTest() {
        Assertions.assertThatThrownBy(() -> new Member(1L, "가이온", null, Role.USER, "password"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("역할이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidRoleTest() {
        Assertions.assertThatThrownBy(() -> new Member(1L, "가이온", "hello@woowa.com", null, "password"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("비밀번호가 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidPasswordTest() {
        Assertions.assertThatThrownBy(() -> new Member(1L, "가이온", "hello@woowa.com", Role.USER, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
