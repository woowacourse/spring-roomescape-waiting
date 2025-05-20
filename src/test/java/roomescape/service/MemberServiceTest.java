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
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.dto.business.UserCreationContent;
import roomescape.dto.response.UserProfileResponse;
import roomescape.exception.local.DuplicatedEmailException;
import roomescape.exception.local.NotFoundUserException;
import roomescape.repository.UserRepository;

@DataJpaTest
class MemberServiceTest {

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
    public class getMemberById {

        @DisplayName("ID를 통해 유저를 조회할 수 있다.")
        @Test
        void canGetUserById() {
            // given
            Member expectedMember = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "password123"));

            entityManager.flush();

            // when
            Member actualMember = userService.getUserById(expectedMember.getId());

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
        Member firstMember = entityManager.persist(
                Member.createWithoutId(Role.GENERAL, "회원", "member1@test.com", "password123"));
        Member secondMember = entityManager.persist(
                Member.createWithoutId(Role.GENERAL, "회원", "member2@test.com", "password123"));

        entityManager.flush();

        // when
        List<UserProfileResponse> allUserProfile = userService.findAllUserProfile();

        // then
        assertThat(allUserProfile)
                .extracting(UserProfileResponse::id)
                .containsExactlyInAnyOrder(firstMember.getId(), secondMember.getId());
    }

    @Nested
    @DisplayName("유저를 추가할 수 있다.")
    public class addMember {

        @DisplayName("유저를 추가할 수 있다.")
        @Test
        void canAddUser() {
            // given
            UserCreationContent creationContent =
                    new UserCreationContent(Role.GENERAL, "회원", "test@test.com", "qwer1234!");

            // when
            UserProfileResponse response = userService.addUser(creationContent);

            // then
            Member expectedMember = entityManager.find(Member.class, response.id());
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(expectedMember.getId()),
                    () -> assertThat(response.name()).isEqualTo(creationContent.name()),
                    () -> assertThat(response.roleName()).isEqualTo(creationContent.role().toString())
            );
        }

        @DisplayName("이메일이 중복인 경우 회원 추가가 불가능하다.")
        @Test
        void cannotAddUser() {
            // given
            Member alreadySavedMember = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member1@test.com", "password123"));

            UserCreationContent creationContent =
                    new UserCreationContent(Role.GENERAL, "회원", alreadySavedMember.getEmail(), "qwer1234!");

            entityManager.flush();

            // when & then
            assertThatThrownBy(() -> userService.addUser(creationContent))
                    .isInstanceOf(DuplicatedEmailException.class)
                    .hasMessage("중복된 이메일입니다.");
        }
    }
}
