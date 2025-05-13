package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.fake.FakeMemberDao;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.repository.MemberDao;
import roomescape.member.service.dto.MemberInfo;

class MemberServiceTest {

    MemberDao memberDao;
    MemberService memberService;

    @BeforeEach
    void init() {
        memberDao = new FakeMemberDao();
        memberService = new MemberService(memberDao);
        memberDao.save(new Member(null, "레오", "레오@gmail.com", "qwer!", MemberRole.ADMIN));
        memberDao.save(new Member(null, "몽이", "몽이@gmail.com", "ㅂㅈㄷㄱ!", MemberRole.ADMIN));
    }

    @DisplayName("모든 멤버 정보를 조회하여 반환할 수 있다")
    @Test
    void findAll() {
        // when
        List<MemberInfo> result = memberService.findAll();

        // then
        assertThat(result).hasSize(2);
    }
}
