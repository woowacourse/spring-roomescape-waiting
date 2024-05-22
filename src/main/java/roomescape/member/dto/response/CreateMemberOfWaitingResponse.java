package roomescape.member.dto.response;

import roomescape.member.domain.Member;

public record CreateMemberOfWaitingResponse(Long id,
                                            String name) {
    public static CreateMemberOfWaitingResponse from(final Member member) {
        return new CreateMemberOfWaitingResponse(
                member.getId(),
                member.getName());
    }
}
