package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberLoginCheckResponse;
import roomescape.member.dto.MemberResponse;
import roomescape.member.exception.MemberExceptionCode;
import roomescape.member.repository.MemberRepository;
import roomescape.member.role.MemberRole;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberLoginCheckResponse findLoginMemberInfo(long id) {
        Member member = memberRepository.findMemberById(id)
                .orElseThrow(() -> new RoomEscapeException(MemberExceptionCode.ID_AND_PASSWORD_NOT_MATCH_OR_EXIST));

        return new MemberLoginCheckResponse(member.getName());
    }

    public List<MemberResponse> findMembersId() {
        List<Member> members = memberRepository.findAll();

        return members.stream()
                .map(member -> new MemberResponse(member.getId()))
                .toList();
    }

    public MemberRole findMemberRole(long id) {
        Member member = memberRepository.findMemberById(id)
                .orElseThrow(() -> new RoomEscapeException(MemberExceptionCode.MEMBER_ROLE_NOT_EXIST_EXCEPTION));

        return MemberRole.findMemberRole(member.getRole().name());
    }
}
