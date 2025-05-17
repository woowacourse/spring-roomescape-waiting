package roomescape.member.service;

import roomescape.member.domain.Member;
import roomescape.member.service.dto.TokenInfo;

public interface TokenProvider {

    String createToken(Member member);

    TokenInfo parsePayload(String token);
}
