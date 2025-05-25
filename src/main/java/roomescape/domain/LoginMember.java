package roomescape.domain;

public record LoginMember(
        Long id,
        String name,
        String email,
        Role role
) {
    public LoginMember(Member member) {
        this(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
    }
}
