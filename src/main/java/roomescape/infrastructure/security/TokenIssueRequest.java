package roomescape.infrastructure.security;

import java.util.Date;
import javax.crypto.SecretKey;

public record TokenIssueRequest(
        Date issuedAt,
        Date expiration,
        Long identifier,
        SecretKey secretKey
) {
}
