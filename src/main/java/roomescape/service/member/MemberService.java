package roomescape.service.member;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.request.SignupRequest;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member signUp(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("사용할 수 없는 이메일 입니다.");
        }
        return memberRepository.save(request.toEntity(request));
    }
}
