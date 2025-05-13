package roomescape.member.dto.response;

import roomescape.global.auth.dto.LoginMember;

public class AuthResponse {

    public record LoginResponse(
            Long id,
            String name,
            String role
    ) {
        public static LoginResponse from(LoginMember loginMember) {
            return new LoginResponse(
                    loginMember.id(),
                    loginMember.name(),
                    loginMember.role().name()
            );
        }
    }
}
