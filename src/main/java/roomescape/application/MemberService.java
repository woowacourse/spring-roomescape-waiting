package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.application.dto.MemberResponse;
import roomescape.application.dto.MemberSignUpRequest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberQueryRepository;

@Service
public class MemberService {
    private final MemberQueryRepository memberQueryRepository;

    public MemberService(MemberQueryRepository memberQueryRepository) {
        this.memberQueryRepository = memberQueryRepository;
    }

    public List<MemberResponse> findAll() {
        List<Member> members = memberQueryRepository.findAll();
        return members.stream()
                .map(MemberResponse::from)
                .toList();
    }

    public MemberResponse save(MemberSignUpRequest memberSignUpRequest) {
        Member member = memberQueryRepository.save(memberSignUpRequest.toEntity());
        return MemberResponse.from(member);
    }
}
