package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberDomainService {

    private final MemberRepository memberRepository;

    public MemberDomainService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member getMember(final Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않은 멤버입니다."));
    }

    public boolean existsByEmail(final String email) {
        return memberRepository.existsByEmail(email);
    }

    public Member save(final Member member) {
        return memberRepository.save(member);
    }

    public List<Member> findByMemberRole(final MemberRole memberRole) {
        return memberRepository.findByMemberRole(memberRole);
    }
}
