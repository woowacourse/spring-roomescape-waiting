package roomescape.service.mapper;

import roomescape.domain.Member;
import roomescape.dto.UserInfo;

public class UserInfoMapper {
    public static UserInfo toResponse(Member member) {
        return new UserInfo(member.getId(), member.getName(), member.getRole().name());
    }
}
