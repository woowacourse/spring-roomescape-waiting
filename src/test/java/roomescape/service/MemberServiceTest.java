package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.repository.MemberDao;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberDao memberDao;

    @InjectMocks
    private MemberService memberService;

    @DisplayName("일반 회원 목록만 조회한다.")
    @Test
    void findUsers() {
        given(memberDao.findByRole(Role.USER)).willReturn(List.of(
                new Member(1L, "roro", "러로", "password", Role.USER)
        ));

        assertThat(memberService.findUsers())
                .extracting(Member::getName)
                .containsExactly("러로");
    }
}
