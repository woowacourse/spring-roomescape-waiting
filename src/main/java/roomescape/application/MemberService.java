package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.application.dto.MemberResponse;
import roomescape.application.dto.MemberSignUpRequest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberCommandRepository;
import roomescape.domain.member.MemberQueryRepository;

@Service
public class MemberService {
    private final MemberQueryRepository memberQueryRepository;
    private final MemberCommandRepository memberCommandRepository;

    public MemberService(MemberQueryRepository memberQueryRepository, MemberCommandRepository memberCommandRepository) {
        this.memberQueryRepository = memberQueryRepository;
        this.memberCommandRepository = memberCommandRepository;
    }

    public List<MemberResponse> findAll() {
        List<Member> members = memberQueryRepository.findAll();
        return members.stream()
                .map(MemberResponse::from)
                .toList();
    }

    public MemberResponse save(MemberSignUpRequest memberSignUpRequest) {
        Member member = memberCommandRepository.save(memberSignUpRequest.toEntity());
        return MemberResponse.from(member);
    }
}
