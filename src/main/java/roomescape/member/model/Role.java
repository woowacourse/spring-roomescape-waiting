package roomescape.member.model;

public enum Role {

    ADMIN,
    NORMAL;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
