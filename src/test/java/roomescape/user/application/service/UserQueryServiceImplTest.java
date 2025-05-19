package roomescape.user.application.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.user.domain.User;
import roomescape.user.domain.UserId;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRepository;
import roomescape.user.domain.UserRole;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class UserQueryServiceImplTest {

    @Autowired
    private UserQueryServiceImpl userQueryService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("유저 Email가 같은 모든 예약을 조회할 수 있다")
    void getByEmail() {
        //given
        UserName userName = UserName.from("강산");
        Email email = Email.from("email@email.com");
        Password password = Password.fromEncoded("1234");
        final User user = userRepository.save(
                User.withoutId(
                        userName,
                        email,
                        password,
                        UserRole.NORMAL));

        UserId userId = user.getId();

        //when
        User actual = userQueryService.getByEmail(email);

        assertThat(actual.getId()).isEqualTo(userId);
        assertThat(actual.getName()).isEqualTo(userName);
        assertThat(actual.getEmail()).isEqualTo(email);
        assertThat(actual.getPassword()).isEqualTo(password);
        assertThat(actual.getRole()).isEqualTo(UserRole.NORMAL);
    }

    @Test
    @DisplayName("모든 유저 정보를 조회할 수 있다")
    void getAll() {
        //given
        UserName userName1 = UserName.from("강산");
        Email email = Email.from("email@email.com");
        Password password = Password.fromEncoded("1234");
        final User user1 = userRepository.save(
                User.withoutId(
                        userName1,
                        email,
                        password,
                        UserRole.NORMAL));
        UserName userName2 = UserName.from("강산2");
        final User user2 = userRepository.save(
                User.withoutId(
                        userName2,
                        email,
                        password,
                        UserRole.NORMAL));

        //when
        List<User> actual = userQueryService.getAll();
        //then
        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.getFirst().getName()).isEqualTo(userName1);
        assertThat(actual.getLast().getName()).isEqualTo(userName2);
    }

    @Test
    @DisplayName("유저 Id를 통해 특정 User를 조회한다")
    void getById() {
        //given
        UserName userName = UserName.from("강산");
        Email email = Email.from("email@email.com");
        Password password = Password.fromEncoded("1234");
        final User user = userRepository.save(
                User.withoutId(
                        userName,
                        email,
                        password,
                        UserRole.NORMAL));

        UserId userId = user.getId();

        //when
        User actual = userQueryService.getById(userId);
        //then
        assertThat(actual.getId()).isEqualTo(userId);
        assertThat(actual.getName()).isEqualTo(userName);
        assertThat(actual.getEmail()).isEqualTo(email);
        assertThat(actual.getPassword()).isEqualTo(password);
        assertThat(actual.getRole()).isEqualTo(UserRole.NORMAL);
    }

    @Test
    @DisplayName("복수의 유저 Id들을 통해 특정 User들을 조회한다")
    void getAllByIds() {
        //given
        UserName userName1 = UserName.from("강산");
        Email email = Email.from("email@email.com");
        Password password = Password.fromEncoded("1234");
        final User user1 = userRepository.save(
                User.withoutId(
                        userName1,
                        email,
                        password,
                        UserRole.NORMAL));
        UserName userName2 = UserName.from("강산2");
        final User user2 = userRepository.save(
                User.withoutId(
                        userName2,
                        email,
                        password,
                        UserRole.NORMAL));
        UserName userName3 = UserName.from("강산3");
        final User user3 = userRepository.save(
                User.withoutId(
                        userName3,
                        email,
                        password,
                        UserRole.NORMAL));

        //when
        List<UserId> ids = List.of(user1.getId(), user2.getId());
        List<User> actual = userQueryService.getAllByIds(ids);
        List<UserId> actualIds = actual.stream()
                .map(User::getId)
                .toList();

        //then
        Assertions.assertThat(actual).hasSize(2);
        Assertions.assertThat(actualIds).containsExactlyInAnyOrderElementsOf(ids);
        Assertions.assertThat(actualIds).doesNotContain(user3.getId());
    }
}
