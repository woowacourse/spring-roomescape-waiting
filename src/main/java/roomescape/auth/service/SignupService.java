package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.RegistrationRequest;
import roomescape.exception.ConflictException;
import roomescape.exception.ExceptionCause;
import roomescape.member.repository.MemberRepository;

@Service
public class SignupService {

    private final MemberRepository memberRepository;

    public SignupService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void signup(RegistrationRequest registrationRequest) {
        if (memberRepository.existsByEmail(registrationRequest.email())) {
            throw new ConflictException(ExceptionCause.MEMBER_DUPLICATE_EMAIL);
        }
        memberRepository.save(registrationRequest.createMember());
    }
}
