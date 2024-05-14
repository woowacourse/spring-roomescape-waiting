package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.dao.MemberDao;
import roomescape.domain.user.Member;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.NotExistException;
import roomescape.service.dto.input.MemberCreateInput;
import roomescape.service.dto.input.MemberLoginInput;
import roomescape.util.DatabaseCleaner;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class MemberServiceTest {
    @Autowired
    MemberService sut;
    @Autowired
    MemberDao memberDao;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("존재하는 이메일과 이메일에 해당하는 비밀번호를 통해 로그인을 하면 성공한다")
    void login_success_with_exist_email_and_equal_password() {
        memberDao.create(Member.fromMember(null, "조이썬", "i894@naver.com", "password1234"));

        final var input = new MemberLoginInput("i894@naver.com", "password1234");
        assertThatCode(() -> sut.loginMember(input))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 이메일을 통해 로그인을 하면 예외가 발생한다.")
    void throw_exception_when_not_exist_email() {
        final var input = new MemberLoginInput("sample@naver.com", "password1234");
        Assertions.assertThatThrownBy(() -> sut.loginMember(input))
                .isInstanceOf(NotExistException.class);
    }

    @Test
    @DisplayName("이메일과 일치하지 않는 비밀번호로 로그인을 하면 예외가 발생한다.")
    void throw_exception_when_not_equal_password() {
        memberDao.create(Member.fromMember(null, "조이썬", "i894@naver.com", "password5678"));

        final var input = new MemberLoginInput("sample@naver.com", "password1234");
        Assertions.assertThatThrownBy(() -> sut.loginMember(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * 필수 요구사항이 아니므로 임의 구현
     */
    @Test
    @DisplayName("멤버를 생성한다.")
    void create_member() {
        final var input = new MemberCreateInput("조이썬", "sample@naver.com", "password1234");
        final var result = sut.createMember(input);
        assertThatCode(() -> memberDao.findById(result.id())
                .get()).doesNotThrowAnyException();
    }
    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시, 예외를 발생한다.")
    void throw_exception_when_already_exist_email() {
        final var input = new MemberCreateInput("조이썬", "sample@naver.com", "password1234");
        sut.createMember(input);
        Assertions.assertThatThrownBy(() -> sut.createMember(input))
                .isInstanceOf(AlreadyExistsException.class);
    }
}
