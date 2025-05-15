package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.persistence.repository.MemberRepository;
import roomescape.presentation.dto.MemberResponse;

@DataJpaTest
@Sql("classpath:data-memberService.sql")
class MemberServiceTest {

    private final MemberService memberService;

    @Autowired
    public MemberServiceTest(final MemberRepository memberRepository) {
        this.memberService = new MemberService(memberRepository);
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
