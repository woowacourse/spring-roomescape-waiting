package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.application.dto.MemberResponse;
import roomescape.domain.Member;
import roomescape.domain.MemberQueryRepository;

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
}
