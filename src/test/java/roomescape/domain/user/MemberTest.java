package roomescape.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class MemberTest {
    @Test
    @DisplayName("정적 팩토리 메소드를 통해 사용자를 생성할 수 있다.")
    void create_domain_with_static_factory_method() {
        assertThatCode(() ->
                Member.fromMember(1l, "조이썬", "i894@naver.com", "password1234"))
                .doesNotThrowAnyException();
    }
}

