package roomescape.service.mapper;

import roomescape.domain.Member;
import roomescape.dto.MemberInfo;

public class MemberInfoMapper {
    public static MemberInfo toResponse(Member member) {
        return new MemberInfo(member.getId(), member.getName(), member.getRole());
    }
}
