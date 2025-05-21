package roomescape.member.controller.dto;

import roomescape.member.service.dto.MemberInfo;

public record MemberResponse(long id, String name) {

    public MemberResponse(final MemberInfo memberInfo) {
        this(memberInfo.id(), memberInfo.name());
    }
}
