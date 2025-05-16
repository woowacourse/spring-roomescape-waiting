package roomescape.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.member.controller.request.SignUpRequest;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.domain.PasswordEncryptor;
import roomescape.member.repository.MemberRepository;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MemberService implements MemberQueryService {

    private final MemberRepository memberRepository;
    private final PasswordEncryptor passwordEncryptor;

    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        boolean exists = memberRepository.existsByEmail(request.email());
        if (exists) {
            throw new InvalidArgumentException("이미 가입된 이메일입니다.");
        }

        Password password = Password.encrypt(request.password(), passwordEncryptor);
        Member signed = Member.signUpUser(request.name(), request.email(), password);
        Member saved = memberRepository.save(signed);
        return MemberResponse.from(saved);
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidArgumentException("존재하지 않는 멤버입니다."));
    }

    public Member getMember(String email, String password) {
        String encryptPassword = passwordEncryptor.encrypt(password);
        return memberRepository.findByEmailAndPassword_Password(email, encryptPassword)
                .orElseThrow(() -> new InvalidArgumentException("가입되지 않은 회원입니다."));
    }

    public List<MemberResponse> getMembers() {
        List<Member> members = memberRepository.findAll();
        return MemberResponse.from(members);
    }
}
