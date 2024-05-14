package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberDao memberDao;
    @InjectMocks
    private MemberService memberService;

    @DisplayName("멤버를 모두 조회할 수 있다.")
    @Test
    void findMembersTest() {
        given(memberDao.findMembers()).willReturn(List.of(
                new Member(1L, "브리", "bri@abc.com"),
                new Member(2L, "브라운", "brown@abc.com")));
        List<MemberResponse> expected = List.of(
                new MemberResponse(1L, "브리"),
                new MemberResponse(2L, "브라운"));

        List<MemberResponse> actual = memberService.findMembers();

        assertThat(actual).isEqualTo(expected);
    }
}
