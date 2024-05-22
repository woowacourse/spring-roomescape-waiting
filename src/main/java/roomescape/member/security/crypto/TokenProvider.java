package roomescape.member.security.crypto;

import java.util.Date;
import java.util.Map;
import roomescape.member.domain.Member;

public interface TokenProvider {

    String createToken(Member member, Date issuedAt);

    Map<String, String> getPayload(String token);

    boolean validateToken(String token);

}
