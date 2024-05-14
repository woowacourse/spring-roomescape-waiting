package roomescape.auth.dto;

import roomescape.auth.JwtTokenProvider;

import java.util.Map;

public record LoginMember(Long id, String email) {

    public static LoginMember from(Map<String, String> claims) {
        Long id = Long.valueOf(claims.get(JwtTokenProvider.CLAIM_ID_KEY));
        return new LoginMember(id, claims.get(JwtTokenProvider.CLAIM_EMAIL_KEY));
    }
}
