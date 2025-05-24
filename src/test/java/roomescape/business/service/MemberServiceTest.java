package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.infrastructure.repository.MemberRepository;
import roomescape.presentation.dto.MemberResponse;

@SpringBootTest
@Transactional
@Sql("classpath:data-memberService.sql")
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

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
