package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import roomescape.common.exception.InvalidInputException;
import roomescape.config.PasswordConfig;
import roomescape.member.dao.MemberJdbcDao;
import roomescape.member.Member;
import roomescape.member.MemberService;
import roomescape.member.web.LoginRequestDto;

@JdbcTest
@Import({MemberService.class, MemberJdbcDao.class, PasswordConfig.class})
@ActiveProfiles("test")
class MemberServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저", "user@test.com", passwordEncoder.encode("password"), "USER"
        );
    }

    @Nested
    class Login {

        @Test
        @DisplayName("인코딩된 비밀번호가 저장된 회원은 원문 비밀번호로 로그인한다")
        void logsInWithRawPassword() {
            LoginRequestDto request = new LoginRequestDto("user@test.com", "password");

            Member member = memberService.login(request);

            assertThat(member.getEmail()).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("원문 비밀번호가 일치하지 않으면 예외를 반환한다")
        void throwsWhenPasswordMismatch() {
            LoginRequestDto request = new LoginRequestDto("user@test.com", "wrong");

            assertThatThrownBy(() -> memberService.login(request))
                    .isInstanceOf(InvalidInputException.class);
        }
    }
}
