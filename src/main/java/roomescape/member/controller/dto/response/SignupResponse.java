package roomescape.member.controller.dto.response;

public record SignupResponse(
        String name,
        String email,
        String password
) {
}
