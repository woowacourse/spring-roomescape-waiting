package roomescape.auth.infrastructure.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("security.jwt.token")
@Getter
@Validated
@RequiredArgsConstructor
public class JwtProperties {

    @NotBlank(message = "JWT 시크릿 키는 비어있을 수 없습니다.")
    private final String secretKey;

    @Positive(message = "JWT 만료 시간은 양수여야 합니다.")
    private final Long expireLength;
}
