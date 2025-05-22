package roomescape.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Users;
import roomescape.business.model.vo.Id;
import roomescape.infrastructure.jpa.JpaUsers;
import roomescape.test_util.JpaTestUtil;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({JpaUsers.class, JpaTestUtil.class})
class UsersTest {

    @Autowired
    private Users sut;
    @Autowired
    private JpaTestUtil testUtil;

    @AfterEach
    void tearDown() {
        testUtil.deleteAll();
    }

    @Test
    void 사용자를_저장하고_조회할_수_있다() {
        // given
        User user = User.member("테스트유저", "test@example.com", "password123");

        // when, then
        assertThatCode(() -> sut.save(user))
                .doesNotThrowAnyException();
    }

    @Test
    void 모든_사용자를_조회할_수_있다() {
        // given
        String userId1 = testUtil.insertUser();
        String userId2 = testUtil.insertUser();

        // when
        final List<User> result = sut.findAll();

        // then
        assertThat(result).extracting(User::getId)
                .containsExactlyInAnyOrder(Id.create(userId1), Id.create(userId2));
    }

    @Nested
    class ID_기준_조회_테스트 {
        
        @Test
        void ID로_사용자를_조회할_수_있다() {
            // given
            String userId = testUtil.insertUser();

            // when
            final Optional<User> result = sut.findById(Id.create(userId));

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId().value()).isEqualTo(userId);
        }

        @Test
        void 존재하지_않는_ID로_사용자를_조회하면_빈_Optional을_반환한다() {
            // when
            final Optional<User> result = sut.findById(Id.create("999"));

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 이메일_기준_조회_테스트 {

        @Test
        void 이메일로_사용자를_조회할_수_있다() {
            // given
            String userId = testUtil.insertUser("돔푸", "dompoo@email.com");

            // when
            final Optional<User> result = sut.findByEmail("dompoo@email.com");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId().value()).isEqualTo(userId);
        }

        @Test
        void 존재하지_않는_이메일로_사용자를_조회하면_빈_Optional을_반환한다() {
            // when
            final Optional<User> result = sut.findByEmail("nonexistent@email.com");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 이메일_존재_확인_테스트 {
        @Test
        void 이메일이_존재하는지_확인할_수_있다() {
            // given
            testUtil.insertUser("돔푸", "dompoo@email.com");

            // when
            final boolean result = sut.existByEmail("dompoo@email.com");

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 존재하지_않는_이메일로_확인하면_false를_반환한다() {
            // when
            final boolean result = sut.existByEmail("nonexistent@email.com");

            // then
            assertThat(result).isFalse();
        }
    }
}
