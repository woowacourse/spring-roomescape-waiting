package roomescape.member.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.exception.MemberNotFound;

@Service
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public MemberQueryService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member findById(final Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new MemberNotFound("멤버를 찾을 수 없습니다."));
    }

    public Member findByEmail(final Email email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFound("멤버를 찾을 수 없습니다."));
    }

    public boolean isExistsByEmail(final Email email) {
        return memberRepository.existsByEmail(email);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }
}
