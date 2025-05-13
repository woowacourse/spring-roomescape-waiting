package roomescape.controller.request;

public record LoginMemberInfo(Long id) {

    public static LoginMemberInfo of(Long id) {
        return new LoginMemberInfo(id);
    }
}
