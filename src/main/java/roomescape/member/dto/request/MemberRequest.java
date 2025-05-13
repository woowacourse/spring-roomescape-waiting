package roomescape.member.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;

public class MemberRequest {

    public record MemberCreateRequest(
            @NotNull String name,
            @NotNull String email,
            @NotNull String password
    ) {
        public Member toEntity() {
            return new Member(0L, name, email, password, RoleType.USER);
        }
    }
}
