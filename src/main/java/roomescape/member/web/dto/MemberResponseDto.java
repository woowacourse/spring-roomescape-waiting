package roomescape.member.web.dto;

import roomescape.member.Member;
import roomescape.member.MemberRole;

public record MemberResponseDto(
        Long id,
        String name,
        String email,
        MemberRole role,
        Long storeId
) {
    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole(),
                member.getStoreId()
        );
    }
}
