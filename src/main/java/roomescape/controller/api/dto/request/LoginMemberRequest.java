package roomescape.controller.api.dto.request;

public record LoginMemberRequest(long id, String email, String password, String name) {
}
