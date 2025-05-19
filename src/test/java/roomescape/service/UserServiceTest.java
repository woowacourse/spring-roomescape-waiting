package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.Role;
import roomescape.domain.User;
import roomescape.dto.business.UserCreationContent;
import roomescape.dto.response.UserProfileResponse;
import roomescape.exception.local.NotFoundUserException;
import roomescape.repository.UserRepository;

@DataJpaTest
class UserServiceTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setup() {
        userService = new UserService(userRepository);
    }

    @Nested
    @DisplayName("ID를 통해 유저를 조회할 수 있다.")
    public class getUserById {

        @DisplayName("ID를 통해 유저를 조회할 수 있다.")
        @Test
        void canGetUserById() {
            // given
            User expectedMember = entityManager.persist(
                    User.createWithoutId(Role.ROLE_MEMBER, "회원", "member@test.com", "password123"));

            entityManager.flush();

            // when
            User actualMember = userService.getUserById(expectedMember.getId());

            // then
            assertThat(actualMember).isEqualTo(expectedMember);
        }

        @DisplayName("유저가 없는 경우 유저를 조회할 수 없다.")
        @Test
        void cannotGetUserById() {
            // given
            long wrongUserId = 100L;

            // when & then
            assertThatThrownBy(() -> userService.getUserById(wrongUserId))
                    .isInstanceOf(NotFoundUserException.class)
                    .hasMessage("해당 유저를 찾을 수 없습니다.");
        }
    }

    @DisplayName("모든 유저의 프로필을 조회할 수 있다.")
    @Test
    void canFindAllUserProfile() {
        // given
        User firstUser = entityManager.persist(
                User.createWithoutId(Role.ROLE_MEMBER, "회원", "member1@test.com", "password123"));
        User secondUser = entityManager.persist(
                User.createWithoutId(Role.ROLE_MEMBER, "회원", "member2@test.com", "password123"));

        entityManager.flush();

        // when
        List<UserProfileResponse> allUserProfile = userService.findAllUserProfile();

        // then
        assertThat(allUserProfile)
                .extracting(UserProfileResponse::id)
                .containsExactlyInAnyOrder(firstUser.getId(), secondUser.getId());
    }

    @DisplayName("유저를 추가할 수 있다.")
    @Test
    void canAddUser() {
        // given
        UserCreationContent creationContent =
                new UserCreationContent(Role.ROLE_MEMBER, "회원", "test@test.com", "qwer1234!");

        // when
        UserProfileResponse response = userService.addUser(creationContent);

        // then
        User expectedUser = entityManager.find(User.class, response.id());
        assertAll(
                () -> assertThat(response.id()).isEqualTo(expectedUser.getId()),
                () -> assertThat(response.name()).isEqualTo(creationContent.name()),
                () -> assertThat(response.roleName()).isEqualTo(creationContent.role().toString())
        );
    }
}
