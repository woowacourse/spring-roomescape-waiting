package roomescape.domain.member.domain;

public enum Role {
    ADMIN("admin"), MEMBER("member");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public String getValue() {
        return value;
    }
}
