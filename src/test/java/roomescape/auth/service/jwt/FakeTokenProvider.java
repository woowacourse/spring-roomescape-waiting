package roomescape.auth.service.jwt;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import roomescape.auth.domain.Payload;
import roomescape.auth.service.TokenProvider;

public class FakeTokenProvider implements TokenProvider {
    Map<String, String> tokens = new HashMap<>();

    @Override
    public String createAccessToken(String payload) {
        String token = createRandomString();
        tokens.put(token, payload);
        return token;
    }

    private String createRandomString() {
        byte[] array = new byte[7];
        new Random().nextBytes(array);
        return new String(array, StandardCharsets.UTF_8);
    }

    @Override
    public Payload<String> getPayload(String token) {
        return new Payload<>(tokens.get(token), () -> isToken(token));
    }

    @Override
    public boolean isToken(String token) {
        return tokens.containsKey(token);
    }
}
