package roomescape.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.member.MemberResponse;

@Sql("/member-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Test
    void 모든_멤버_조회() {
        //when
        List<MemberResponse> allMembers = memberService.findAllMembers();

        //then
        assertThat(allMembers).hasSize(2);
    }

    @Test
    void 존재하지_않는_id로_조회할_경우_예외_발생() {
        //given, when, then
        List<MemberResponse> allMembers = memberService.findAllMembers();
        Long notExistId = allMembers.size() + 1L;

        //when, then
        assertThatThrownBy(() -> memberService.getMemberById(notExistId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
