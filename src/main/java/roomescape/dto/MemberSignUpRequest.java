package roomescape.dto;

import jakarta.validation.constraints.NotNull;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.Password;
import roomescape.domain.member.PlayerName;
import roomescape.domain.member.Role;

public record MemberSignUpRequest(
        @NotNull
        String name,
        @NotNull
        String email,
        @NotNull
        String password
) {
    public Member toEntity() {
        return new Member(new PlayerName(name), new Email(email), new Password(password), Role.BASIC);
    }
}
