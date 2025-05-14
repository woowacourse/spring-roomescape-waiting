package roomescape.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.controller.request.SignUpRequest;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.member.service.out.MemberRepository;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse signUp(SignUpRequest request) {
        boolean exists = memberRepository.existsByEmail(request.email());
        if (exists) {
            throw new IllegalArgumentException("[ERROR] 이미 가입된 이메일입니다.");
        }
        Member signed = Member.signUpUser(request.name(), request.email(), request.password());
        Member saved = memberRepository.save(signed);
        return MemberResponse.from(saved);
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 멤버입니다."));
    }

    public Member getMember(String email, String password) {
        return memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 가입되지 않은 회원입니다."));
    }

    public List<MemberResponse> getMembers() {
        List<Member> members = memberRepository.findAll();
        return MemberResponse.from(members);
    }
}
