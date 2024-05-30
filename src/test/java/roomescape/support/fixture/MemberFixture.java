package roomescape.support.fixture;

import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;

@Component
public class MemberFixture {

    private final MemberRepository memberRepository;

    public MemberFixture(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member createMember(String email) {
        Member member = new Member("eden", email, "12341234", Role.MEMBER);
        return memberRepository.save(member);
    }

    public Member createMember(String email, String password) {
        Member member = new Member("eden", email, password, Role.MEMBER);
        return memberRepository.save(member);
    }

    public Member createMember() {
        Member member = new Member("eden", "eden@eden.com", "12341234", Role.MEMBER);
        return memberRepository.save(member);
    }

    public Member createAdmin() {
        Member member = new Member("admin", "admin@admin.com", "12341234", Role.ADMIN);
        return memberRepository.save(member);
    }

    public Member createAdmin(String email, String password) {
        Member member = new Member("eden", email, password, Role.ADMIN);
        return memberRepository.save(member);
    }
}
