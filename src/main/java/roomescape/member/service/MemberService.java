package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.dto.MemberLoginCheckResponse;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.member.role.MemberRole;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberLoginCheckResponse findLoginMemberInfo(long id) {
        String name = memberRepository.findNameById(id);
        return new MemberLoginCheckResponse(name);
    }

    public List<MemberResponse> findMembersId() {
        List<Long> ids = memberRepository.findAllId();

        return ids.stream()
                .map(MemberResponse::new)
                .toList();
    }

    public MemberRole findMemberRole(long id) {
        return memberRepository.findRoleById(id);
    }
}
