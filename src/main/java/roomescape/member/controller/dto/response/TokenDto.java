package roomescape.member.controller.dto.response;

public record TokenDto(
    String token
) {

    public static TokenDto from(String token) {
        return new TokenDto(token);
    }
}
