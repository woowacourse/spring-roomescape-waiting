package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberExceptionCode;
import roomescape.member.exception.model.MemberNotFoundException;
import roomescape.member.repository.MemberRepository;
import roomescape.member.role.MemberRole;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findMembersId() {
        return memberRepository.findAll();
    }

    public MemberRole findMemberRole(long id) {
        Member member = memberRepository.findMemberById(id)
                .orElseThrow(() -> new RoomEscapeException(MemberExceptionCode.MEMBER_ROLE_NOT_EXIST_EXCEPTION));

        return MemberRole.findMemberRole(member.getRole().name());
    }

    public Member findMember(long memberId) {
        return memberRepository.findMemberById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }
}
