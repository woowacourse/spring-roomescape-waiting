package roomescape.controller.member.dto;

public record CookieMemberResponse(String name) {

    public static final CookieMemberResponse NON_LOGIN = new CookieMemberResponse(null);
}
