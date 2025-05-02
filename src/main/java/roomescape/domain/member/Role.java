package roomescape.domain.member;

public enum Role {

    USER,
    ADMIN;

    public boolean isAdmin() {
        return this.equals(ADMIN);
    }
}
