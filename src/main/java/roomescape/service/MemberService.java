package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.entity.Member;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.request.SignupRequest;
import roomescape.exception.InvalidMemberException;
import roomescape.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member addMember(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new InvalidMemberException("동일한 이메일로 추가할 수 없습니다.");
        }
        return memberRepository.add(request.toMember());
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findByEmailAndPassword(LoginRequest request) {
        return memberRepository.findByEmailAndPassword(request.email(), request.password())
                .orElseThrow(() -> new InvalidMemberException("유효하지 않은 로그인 정보입니다."));
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 멤버 ID입니다."));
    }
}
