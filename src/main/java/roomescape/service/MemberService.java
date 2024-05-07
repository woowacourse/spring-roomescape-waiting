package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.MemberRepository;
import roomescape.service.dto.request.MemberRequest;
import roomescape.service.dto.response.MemberResponse;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberResponse createMember(MemberRequest memberRequest) {
        return MemberResponse.from(memberRepository.save(memberRequest.toEntity()));
    }

    public List<MemberResponse> findAllMembers() {
        return memberRepository.findAllMembers().stream()
                .map(MemberResponse::from)
                .toList();
    }
}
