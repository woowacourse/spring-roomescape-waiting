package roomescape.domain;

public enum Role {
    MEMBER("MEMBER"),
    MANAGER("MANAGER");

    private final String name;

    Role(String name) {
        this.name = name;
    }
}
