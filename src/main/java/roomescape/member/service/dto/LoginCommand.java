package roomescape.member.service.dto;

public record LoginCommand(
        String name,
        String password
) {
}
