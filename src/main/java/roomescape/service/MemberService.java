package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.exception.member.NotFoundMemberException;
import roomescape.service.dto.response.MemberResponse;

@Service
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> findAllMember() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(MemberResponse::new)
                .toList();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(NotFoundMemberException::new);
    }
}
