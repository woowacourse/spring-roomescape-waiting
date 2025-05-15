package roomescape.member.service;

import java.util.List;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Member;

public interface MemberQueryService {

    Member getMember(String email, String password);

    Member getMember(Long memberId);

    List<MemberResponse> getMembers();
}
