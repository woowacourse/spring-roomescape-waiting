package roomescape.domain.member.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.member.application.dto.response.MemberServiceResponse;
import roomescape.domain.member.model.Member;
import roomescape.domain.member.model.MemberRepository;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public List<MemberServiceResponse> getAll() {
        List<Member> members = memberRepository.getAll();
        return members.stream()
                .map(MemberServiceResponse::from)
                .toList();
    }
}
