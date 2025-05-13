package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import roomescape.dto.member.RegistrationRequest;
import roomescape.exception.DuplicateContentException;
import roomescape.repository.MemberRepository;

@Service
public class SignupService {

    private final MemberRepository memberRepository;

    public SignupService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void signup(RegistrationRequest registrationRequest) {
        try {
            memberRepository.save(registrationRequest.createRegistrationDetails());
        } catch (DuplicateKeyException e) {
            throw new DuplicateContentException("[ERROR] 이미 가입한 이메일입니다.");
        }
    }
}
