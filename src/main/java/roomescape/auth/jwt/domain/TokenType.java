package roomescape.auth.jwt.domain;

import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public enum TokenType {
    ACCESS(Duration.ofMinutes(10)),
    ;

    private final Duration period;

    public String getDescription() {
        return this.name() + "_TOKEN";
    }

    public int getPeriodInMillis() {
        return Math.toIntExact(period.toMillis());
    }

    public int getPeriodInSeconds() {
        return Math.toIntExact(period.getSeconds());
    }
}

