package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberDao;
import roomescape.utils.JdbcTemplateUtils;

@JdbcTest
@Import({MemberDao.class, MemberService.class})
class MemberServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private MemberService memberService;

    @AfterEach
    void tearDown() {
        JdbcTemplateUtils.deleteAllTables(jdbcTemplate);
    }

    @DisplayName("이메일과 패스워드를 알려주면 일치하는 사용자를 찾아온다.")
    @Test
    void findMemberByEmailAndPassword() {
        String email = "if@posty.com";
        String password = "12345678";
        memberDao.save(new Member("이프", email, password, Role.MEMBER));

        Member findMember = memberService.findMemberByEmailAndPassword(email, password);

        assertAll(
                () -> assertThat(findMember.getEmail()).isEqualTo(email),
                () -> assertThat(findMember.getPassword()).isEqualTo(password)
        );
    }

    @DisplayName("이메일 또는 패스워드가 맞지 않으면 사용자를 찾아올 수 없다.")
    @CsvSource(value = {"posty@if.com:12345678", "if@posty.com:87654321"}, delimiter = ':')
    @ParameterizedTest
    void findMemberByUnmatchedEmailAndPassword(String unmatchedEmail, String unmatchedPassword) {
        String email = "if@posty.com";
        String password = "12345678";
        memberDao.save(new Member("이프", email, password, Role.MEMBER));

        assertThatThrownBy(() -> memberService.findMemberByEmailAndPassword(unmatchedEmail, unmatchedPassword))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
