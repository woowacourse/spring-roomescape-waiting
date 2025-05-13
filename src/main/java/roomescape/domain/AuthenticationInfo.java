package roomescape.domain;

public record AuthenticationInfo(
    long id,
    UserRole role
) {

    public boolean isAdmin() {
        return UserRole.ADMIN == this.role;
    }
}
