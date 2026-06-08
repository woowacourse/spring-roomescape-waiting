package roomescape.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("사용자")
class UserTest {

    @Test
    @DisplayName("생성하면 식별자 없이 이름만 가진다")
    void create() {
        // when
        User user = User.create("홍길동");

        // then
        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("조회 결과를 그대로 보관한다")
    void of() {
        // when
        User user = User.of(1L, "김철수");

        // then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("김철수");
    }
}
