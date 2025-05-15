package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.RegistrationRequest;
import roomescape.exception.DuplicateContentException;
import roomescape.member.repository.MemberRepository;

@Service
public class SignupService {

    private final MemberRepository memberRepository;

    public SignupService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void signup(RegistrationRequest registrationRequest) {
        if (memberRepository.existsByEmail(registrationRequest.email())) {
            throw new DuplicateContentException("[ERROR] 이미 가입한 이메일입니다.");
        }
        memberRepository.save(registrationRequest.createMember());
    }
}
