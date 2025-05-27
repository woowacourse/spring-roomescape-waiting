package roomescape.member.domain;

public enum Role {
    ADMIN,
    USER;

    public static Role from(String value) {
        return Role.valueOf(value.toUpperCase());
    }
}

