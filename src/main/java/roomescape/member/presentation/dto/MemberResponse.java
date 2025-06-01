package roomescape.member.presentation.dto;

import roomescape.member.application.dto.MemberInfo;

public record MemberResponse(long id, String name) {

    public MemberResponse(final MemberInfo memberInfo) {
        this(memberInfo.id(), memberInfo.name());
    }
}
