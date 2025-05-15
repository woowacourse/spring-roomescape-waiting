package roomescape.unit.member.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.fake.FakeMemberRepository;
import roomescape.member.application.service.MemberService;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.domain.MemberRepository;
import roomescape.member.application.dto.MemberInfo;

class MemberServiceTest {

    private MemberService memberService;

    @BeforeEach
    void init() {
        final MemberRepository memberRepository = new FakeMemberRepository();
        memberService = new MemberService(memberRepository);
        memberRepository.save(new Member(null, "레오", "레오@gmail.com", "qwer!", MemberRole.ADMIN));
        memberRepository.save(new Member(null, "몽이", "몽이@gmail.com", "ㅂㅈㄷㄱ!", MemberRole.ADMIN));
    }

    @DisplayName("모든 멤버 정보를 조회하여 반환할 수 있다")
    @Test
    void findAll() {
        // when
        final List<MemberInfo> result = memberService.findAll();

        // then
        assertThat(result).hasSize(2);
    }
}
