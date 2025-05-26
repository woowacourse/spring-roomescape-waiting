package roomescape.member.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.request.MemberCreationRequest;
import roomescape.auth.dto.response.MemberCreationUpResponse;
import roomescape.member.dto.response.MemberNameResponse;

@Service
@AllArgsConstructor
public class MemberServiceFacade {

    private final MemberService memberService;

    public MemberCreationUpResponse create(MemberCreationRequest request) {
        return memberService.create(request);
    }

    public List<MemberNameResponse> findNames() {
        return memberService.findNames();
    }
}
