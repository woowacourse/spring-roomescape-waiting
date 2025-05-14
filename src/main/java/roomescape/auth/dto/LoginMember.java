package roomescape.auth.dto;

import lombok.NonNull;
import roomescape.member.domain.MemberRole;

public record LoginMember(
        @NonNull Long id,
        @NonNull String name,
        @NonNull String email,
        @NonNull MemberRole role
) {
}
