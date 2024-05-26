package roomescape.auth.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.RandomStringUtils;
import roomescape.auth.domain.Token;
import roomescape.auth.provider.model.TokenProvider;

public class FakeTokenProvider implements TokenProvider {

    private static final int FAKE_TOKEN_LENGTH = 10;

    private final Map<Long, String> tokenStorage = new ConcurrentHashMap<>();

    public FakeTokenProvider() {
    }

    @Override
    public Token getAccessToken(long principal) {
        String token = makeRandomToken();
        tokenStorage.put(principal, token);
        return new Token(token);
    }

    @Override
    public String resolveToken(String token) {
        return tokenStorage.values().stream()
                .filter(storageToken -> storageToken.equals(token))
                .findAny()
                .orElse(null);
    }

    public String makeRandomToken() {
        return RandomStringUtils.randomAlphanumeric(FAKE_TOKEN_LENGTH);
    }
}
