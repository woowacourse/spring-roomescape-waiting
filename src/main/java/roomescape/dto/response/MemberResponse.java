package roomescape.dto.response;

import roomescape.auth.Role;
import roomescape.domain.Member;

public record MemberResponse(
        Long id,
        String email,
        String name,
        Role role,
        Long storeId
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getStoreId()
        );
    }
}
