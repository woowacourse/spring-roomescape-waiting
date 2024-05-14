package roomescape.member.dto.response;

import roomescape.member.domain.Member;

public record CreateMemberOfReservationResponse(Long id,
                                                String name) {
    public static CreateMemberOfReservationResponse from(final Member member) {
        return new CreateMemberOfReservationResponse(
                member.getId(),
                member.getName());
    }
}
