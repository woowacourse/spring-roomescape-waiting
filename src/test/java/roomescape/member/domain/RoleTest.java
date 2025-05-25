package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

    @DisplayName("이름을 기반으로 객체를 반환한다")
    @Test
    void findBy() {
        // given
        String name = "user";

        // when
        Role actual = Role.findBy(name);

        // then
        assertThat(actual).isEqualTo(Role.USER);
    }
}