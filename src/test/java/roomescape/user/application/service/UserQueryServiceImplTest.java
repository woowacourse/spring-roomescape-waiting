package roomescape.user.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.application.dto.CreateUserRequest;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.common.exception.NotFoundException;
import roomescape.user.domain.User;
import roomescape.user.domain.UserId;
import roomescape.user.domain.UserName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
class UserQueryServiceImplTest {

    @Autowired
    private UserQueryService userQueryService;
    
    @Autowired
    private UserCommandService userCommandService;
    
    private User user1;
    private User user2;
    
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        final UserName userName1 = UserName.from("사용자1");
        final Email email1 = Email.from("user1@example.com");
        final Password password1 = Password.fromEncoded("encodedPassword1");
        final CreateUserRequest request1 = new CreateUserRequest(userName1, email1, password1);
        user1 = userCommandService.create(request1);
        
        final UserName userName2 = UserName.from("사용자2");
        final Email email2 = Email.from("user2@example.com");
        final Password password2 = Password.fromEncoded("encodedPassword2");
        final CreateUserRequest request2 = new CreateUserRequest(userName2, email2, password2);
        user2 = userCommandService.create(request2);
    }

    @Test
    @DisplayName("이메일로 사용자를 조회한다")
    void getByEmail() {
        // given
        final Email email = user1.getEmail();

        // when
        final User result = userQueryService.getByEmail(email);

        // then
        assertAll(() -> {
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getName()).isEqualTo(user1.getName());
        });
    }

    @Test
    @DisplayName("이메일로 사용자가 존재하지 않으면 예외가 발생한다")
    void getByEmailNotFound() {
        // given
        final Email nonExistentEmail = Email.from("nonexistent@example.com");

        // when
        // then
        assertThatThrownBy(() -> userQueryService.getByEmail(nonExistentEmail))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("[USER] not found. params={Email=Email(value=nonexistent@example.com)}");
    }

    @Test
    @DisplayName("모든 사용자를 조회한다")
    void getAll() {
        // when
        final List<User> result = userQueryService.getAll();

        // then
        assertAll(() -> {
            assertThat(result).isNotEmpty();
            assertThat(result).extracting(User::getEmail)
                    .contains(user1.getEmail(), user2.getEmail());
        });
    }

    @Test
    @DisplayName("ID 리스트로 사용자들을 조회한다")
    void getAllByIds() {
        // given
        final List<UserId> ids = List.of(user1.getId(), user2.getId());

        // when
        final List<User> result = userQueryService.getAllByIds(ids);

        // then
        assertAll(() -> {
            assertThat(result).hasSize(2);
            assertThat(result).extracting(User::getId)
                    .containsExactlyInAnyOrder(user1.getId(), user2.getId());
        });
    }

    @Test
    @DisplayName("ID로 사용자를 조회한다")
    void getById() {
        // given
        final UserId id = user1.getId();

        // when
        final User result = userQueryService.getById(id);

        // then
        assertAll(() -> {
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getName()).isEqualTo(user1.getName());
            assertThat(result.getEmail()).isEqualTo(user1.getEmail());
        });
    }

    @Test
    @DisplayName("ID로 사용자가 존재하지 않으면 예외가 발생한다")
    void getByIdNotFound() {
        // given
        final UserId nonExistentId = UserId.from(999L);

        // when
        // then
        assertThatThrownBy(() -> userQueryService.getById(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");
    }
} 
