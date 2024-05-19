package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialMemberFixture.MEMBER_2;
import static roomescape.InitialMemberFixture.MEMBER_3;
import static roomescape.InitialMemberFixture.MEMBER_4;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.dto.MemberIdNameResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("모든 Member들의 id와 name을 조회한다.")
    void findMembersIdAndName() {
        List<MemberIdNameResponse> expected = new ArrayList<>();
        expected.add(new MemberIdNameResponse(MEMBER_1));
        expected.add(new MemberIdNameResponse(MEMBER_2));
        expected.add(new MemberIdNameResponse(MEMBER_3));
        expected.add(new MemberIdNameResponse(MEMBER_4));

        List<MemberIdNameResponse> found = memberService.findMembersIdAndName();

        assertThat(found).isEqualTo(expected);
    }
}
