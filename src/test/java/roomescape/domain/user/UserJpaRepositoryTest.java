package roomescape.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixtures.anyUser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.exception.NotFoundException;

@DataJpaTest
class UserJpaRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자를 조회한다.")
    void findByEmail() {
        // given
        var name = new UserName("포포");
        var email = new Email("popo@email.com");
        var password = new Password("123456");
        var saved = userRepository.save(new User(name, email, password));

        // when
        var found = userRepository.findByEmail(new Email("popo@email.com"));

        // then
        assertThat(found).hasValue(saved);
    }

    @Test
    @DisplayName("아이디로 사용자를 조회한다.")
    void getById() {
        // given
        var saved = userRepository.save(anyUser());

        // when
        var found = userRepository.getById(saved.id());

        // then
        assertThat(found).isEqualTo(saved);
    }

    @Test
    @DisplayName("사용자 조회 시 해당 아이디의 사용자가 없으면 예외가 발생한다.")
    void getByIdWhenNotFound() {
        assertThatThrownBy(() -> userRepository.getById(1234L))
            .isInstanceOf(NotFoundException.class);
    }
}
