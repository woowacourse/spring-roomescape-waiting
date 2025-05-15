package roomescape.member.domain;

import lombok.Getter;

@Getter
public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String value;

    Role(final String value) {
        this.value = value;
    }
}
