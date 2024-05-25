package roomescape.helper.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.MemberRole;

@Component
public class MemberFixture {
    @Autowired
    private MemberRepository memberRepository;

    public Member createUserMember() {
        Member user = new Member(
                new MemberName("사용자"),
                new MemberEmail("user@gmail.com"),
                new MemberPassword("1234567890"),
                MemberRole.USER
        );
        return memberRepository.save(user);
    }

    public Member createAdminMember() {
        Member admin = new Member(
                new MemberName("관리자"),
                new MemberEmail("admin@gmail.com"),
                new MemberPassword("1234567890"),
                MemberRole.ADMIN
        );
        return memberRepository.save(admin);
    }
}
