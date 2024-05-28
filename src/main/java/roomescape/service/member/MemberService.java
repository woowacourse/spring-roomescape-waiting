package roomescape.service.member;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.exception.InvalidMemberException;
import roomescape.service.member.dto.MemberResponse;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::new)
                .toList();
    }

    public Member findById(long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 회원입니다."));
    }
}
