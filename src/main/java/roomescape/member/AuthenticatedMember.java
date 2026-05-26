package roomescape.member;

public record AuthenticatedMember(
        Long id,
        Role role
) {
    public static AuthenticatedMember of(long id, Role role) {
        return new AuthenticatedMember(id, role);
    }

    public boolean isManager() {
        return role == Role.MANAGER;
    }
}
