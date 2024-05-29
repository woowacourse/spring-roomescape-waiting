package roomescape.domain.member;

public enum Role {
    ADMIN, USER;

    public boolean isNotAdmin() {
        return this != ADMIN;
    }
}
