package roomescape.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.presentation.dto.MemberRequest;

@Service
public class MemberCommandService {

    private final MemberRepository memberRepository;

    public MemberCommandService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member save(final MemberRequest request) {
        return memberRepository.save(new Member(
            new Name(request.name()), new Email(request.email()), new Password(request.password()))
        );
    }
}
