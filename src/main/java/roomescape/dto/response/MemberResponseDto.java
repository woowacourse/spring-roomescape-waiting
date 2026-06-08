package roomescape.dto.response;

import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;

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
