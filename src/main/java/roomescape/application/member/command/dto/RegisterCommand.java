package roomescape.application.member.command.dto;

public record RegisterCommand(
        String email,
        String password,
        String name
) {
}
