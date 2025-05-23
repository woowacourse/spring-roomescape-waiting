package roomescape.admin.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.admin.domain.Admin;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaAdminRepositoryTest {

    @Autowired
    private JpaAdminRepository jpaAdminRepository;

    @Test
    void 이메일로_Admin을_조회할_수_있다() {
        Optional<Admin> found = jpaAdminRepository.findByEmail("admin@email.com");

        assertThat(found.get().getEmail()).isEqualTo("admin@email.com");
    }

    @Test
    void 이메일_존재여부를_확인할_수_있다() {
        boolean exists = jpaAdminRepository.existsByEmail("admin@email.com");

        assertThat(exists).isTrue();
    }

    @Test
    void 존재하지_않는_이메일은_false를_반환한다() {
        boolean exists = jpaAdminRepository.existsByEmail("no@sample.com");

        assertThat(exists).isFalse();
    }
}