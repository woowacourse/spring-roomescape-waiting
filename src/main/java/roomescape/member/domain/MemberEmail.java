package roomescape.member.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public record MemberEmail(String email) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[0-9a-zA-Z]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$");

    public MemberEmail {
        Objects.requireNonNull(email);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("이메일 형식이 일치하지 않습니다.");
        }
    }
}
