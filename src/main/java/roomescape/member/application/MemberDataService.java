package roomescape.member.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.infrastructure.MemberRepository;

@Service
public class MemberDataService {

    private final MemberRepository memberRepository;

    public MemberDataService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member create(final Member member) {
        return memberRepository.save(member);
    }

    public Member getById(final Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않은 멤버입니다."));
    }

    public List<Member> findByMemberRole(final MemberRole memberRole) {
        return memberRepository.findByMemberRole(memberRole);
    }

    public boolean existsByEmail(final String email) {
        return memberRepository.existsByEmail(email);
    }

    public void validateExists(final Long memberId) {
        boolean doesExists = memberRepository.existsById(memberId);
        if (!doesExists) {
            throw new MemberNotFoundException("존재하지 않은 멤버입니다.");
        }
    }
}
