package roomescape.service.member;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.repository.MemberRepository;
import roomescape.service.member.dto.MemberRequest;
import roomescape.service.member.dto.MemberResponse;

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
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }
}
