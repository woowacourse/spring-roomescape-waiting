package roomescape.integration.fixture;

import static roomescape.integration.fixture.MemberEmailFixture.이메일_leehyeonsu4888지메일;
import static roomescape.integration.fixture.MemberNameFixture.한스;
import static roomescape.integration.fixture.MemberPasswordFixture.비밀번호_gustn111느낌표두개;

import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.repository.MemberRepository;

@Component
public class MemberDbFixture {

    private final MemberRepository memberRepository;

    public MemberDbFixture(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member 한스_leehyeonsu4888_지메일_일반_멤버() {
        return createMember(한스, 이메일_leehyeonsu4888지메일, 비밀번호_gustn111느낌표두개, MemberRole.MEMBER);
    }

    public Member leehyeonsu4888_지메일_gustn111느낌표두개() {
        return createMember(한스, 이메일_leehyeonsu4888지메일, 비밀번호_gustn111느낌표두개, MemberRole.MEMBER);
    }


    public Member createMember(
            final MemberName name,
            final MemberEmail email,
            final MemberEncodedPassword password,
            final MemberRole role
    ) {
        return memberRepository.save(new Member(null, name, email, password, role));
    }
}
