package roomescape.application.provider;


import java.util.Date;

public interface JwtTokenProvider {

    String createToken(String payload);

    String createToken(String payload, Date now);

    String getPayload(String token);

    void validateToken(String token);
}
