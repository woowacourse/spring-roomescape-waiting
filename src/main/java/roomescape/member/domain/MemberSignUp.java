package roomescape.member.domain;

public record MemberSignUp(String name, String email, String password, Role role) {
}
