package roomescape.dto;

public record LoginResponse(String name) {
    public static LoginResponse from(LoginMemberRequest loginMemberRequest) {
        return new LoginResponse(loginMemberRequest.name().getValue());
    }
}
