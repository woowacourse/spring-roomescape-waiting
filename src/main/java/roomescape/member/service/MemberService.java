package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.dto.response.FindMembersResponse;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<FindMembersResponse> getMembers() {
        return memberRepository.findAll().stream()
                .map(FindMembersResponse::of)
                .toList();
    }
}
