package roomescape.infra.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.user.User;

@DisplayName("사용자 JDBC 저장소")
@JdbcTest(properties = "spring.sql.init.mode=always")
@Import(JdbcUserRepository.class)
class JdbcUserRepositoryTest {

    @Autowired
    private JdbcUserRepository userRepository;

    @DisplayName("사용자를 저장할 수 있다")
    @Test
    void save() {
        // given
        User saved = userRepository.save(User.create("테스트홍길동"));

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스트홍길동");
    }

    @DisplayName("이름으로 사용자를 조회할 수 있다")
    @Test
    void findByName() {
        // given
        userRepository.save(User.create("테스트김철수"));

        // when & then
        assertThat(userRepository.findByName("테스트김철수"))
                .hasValueSatisfying(user -> {
                    assertThat(user.getName()).isEqualTo("테스트김철수");
                });
    }

    @DisplayName("이름 존재 여부를 확인할 수 있다")
    @Test
    void existsByName() {
        // given
        userRepository.save(User.create("테스트이름"));

        // when & then
        assertThat(userRepository.existsByName("테스트이름")).isTrue();
        assertThat(userRepository.existsByName("없는이름")).isFalse();
    }
}
