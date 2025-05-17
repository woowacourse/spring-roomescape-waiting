package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.enums.Role;
import roomescape.exception.RoomEscapeException;

public class MemberTest {

    @DisplayName("멤버는 빈 이름으로 생성할 수 없다")
    @Test
    void memberNameTest() {
        // given
        Long id = 1L;
        String name = null;
        String email = "email";
        String password = "password";
        Role role = Role.ADMIN;
        // when & then
        assertThatThrownBy(() -> new Member(id, name, email, password, role)).isInstanceOf(RoomEscapeException.class);
    }

    @DisplayName("멤버는 빈 이메일로 생성할 수 없다")
    @Test
    void memberEamilTest() {
        // given
        Long id = 1L;
        String name = "name";
        String email = null;
        String password = "password";
        Role role = Role.ADMIN;
        // when & then
        assertThatThrownBy(() -> new Member(id, name, email, password, role)).isInstanceOf(RoomEscapeException.class);
    }

    @DisplayName("멤버는 빈 패스워드로 생성할 수 없다")
    @Test
    void memberPasswordTest() {
        // given
        Long id = 1L;
        String name = "name";
        String email = "email";
        String password = null;
        Role role = Role.ADMIN;
        // when & then
        assertThatThrownBy(() -> new Member(id, name, email, password, role)).isInstanceOf(RoomEscapeException.class);
    }

    @DisplayName("멤버는 빈 역할로 생성할 수 없다")
    @Test
    void memberRoleTest() {
        // given
        Long id = 1L;
        String name = "name";
        String email = "email";
        String password = "password";
        Role role = null;
        // when & then
        assertThatThrownBy(() -> new Member(id, name, email, password, role)).isInstanceOf(RoomEscapeException.class);
    }
}
