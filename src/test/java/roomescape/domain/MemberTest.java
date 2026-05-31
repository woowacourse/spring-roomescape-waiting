package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberTest {

    @Test
    @DisplayName("이름이 같으면 동등한 회원이다.")
    void equality() {
        assertThat(new Member("me")).isEqualTo(new Member("me"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    @DisplayName("이름이 null이거나 비어 있으면 회원을 만들 수 없다.")
    void rejectBlank(String name) {
        assertThatThrownBy(() -> new Member(name))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
