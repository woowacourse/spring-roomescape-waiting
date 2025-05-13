package roomescape.repository;

import roomescape.util.TokenProvider;

public class FakeTokenProvider implements TokenProvider {

    @Override
    public String createToken(String payload) {
        return payload;
    }

    @Override
    public String getPayload(String token) {
        return token;
    }

    @Override
    public boolean validateToken(String token) {
        return true;
    }
}
