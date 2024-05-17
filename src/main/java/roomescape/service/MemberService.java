package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.exception.member.NotFoundMemberException;
import roomescape.service.dto.MemberListResponse;
import roomescape.service.dto.MemberResponse;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public MemberListResponse findAllMember() {
        List<Member> members = memberRepository.findAll();
        return new MemberListResponse(members.stream()
                .map(MemberResponse::new)
                .toList());
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(NotFoundMemberException::new);
    }
}
