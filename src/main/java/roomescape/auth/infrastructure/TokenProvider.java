package roomescape.auth.infrastructure;

import io.jsonwebtoken.Claims;
import roomescape.exception.TokenCreationException;

public interface TokenProvider {

    /**
     * +     * 주어진 Claims 정보로 토큰을 생성합니다. +     * +     * @param claims 토큰에 포함될 정보 +     * @return 생성된 토큰 문자열 +     *
     *
     * @throws TokenCreationException 토큰 생성 중 오류가 발생할 경우 +
     */
    String createToken(final Claims claims);

    /**
     * +     * 토큰에서 주요 정보(사용자 식별자 등)를 추출합니다. +     * +     * @param token 검증할 토큰 문자열 +     * @return 토큰에서 추출한 주요 정보(사용자
     * 식별자 등) +     * @throws UnauthorizedException 유효하지 않은 토큰인 경우 +
     */
    String extractPrincipal(final String token);
}
