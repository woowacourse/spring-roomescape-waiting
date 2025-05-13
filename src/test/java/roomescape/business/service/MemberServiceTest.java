package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.persistence.dao.JdbcMemberDao;
import roomescape.persistence.dao.MemberDao;
import roomescape.presentation.dto.MemberResponse;

@JdbcTest
@Sql("classpath:data-memberService.sql")
class MemberServiceTest {

    private final MemberService memberService;

    @Autowired
    public MemberServiceTest(final JdbcTemplate jdbcTemplate) {
        final MemberDao memberDao = new JdbcMemberDao(jdbcTemplate);
        this.memberService = new MemberService(memberDao);
    }

    @Test
    @DisplayName("모든 사용자를 조회한다")
    void findAll() {
        // given
        // data-memberService.sql
        // 4명의 사용자가 주어진다.

        // when
        List<MemberResponse> memberResponses = memberService.findAll();

        // then
        assertThat(memberResponses).hasSize(4);
    }
}
