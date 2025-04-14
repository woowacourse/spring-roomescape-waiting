package roomescape.member.service;

import org.springframework.stereotype.Service;
import roomescape.member.controller.request.SignUpRequest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member save(SignUpRequest request) {
        Email email = new Email(request.email());
        Name name = new Name(request.name());
        Password password = new Password(request.password());

        return memberRepository.save(name, email, password);
    }

    public Member findById(Long id) {
        return memberRepository.findMemberById(id);
    }

    public Member findByName(String name) {
        return memberRepository.findMemberByName(name);
    }
}
