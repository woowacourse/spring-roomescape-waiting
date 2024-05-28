package roomescape.dto;

import roomescape.domain.Member;

public record MemberPreviewResponse(Long id, String name) {

    public static MemberPreviewResponse from(Member member) {
        return new MemberPreviewResponse(member.getId(), member.getName());
    }
}
