package roomescape.member.service.dto;

public record MemberSaveCommand(
    String name,
    String password
) {

}
