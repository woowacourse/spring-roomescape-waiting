package roomescape.waiting.dto;

import java.time.LocalDateTime;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.waiting.domain.Waiting;

public record WaitingResponse(
        Long id,
        ReservationResponse reservation,
        MemberResponse member,
        LocalDateTime createdAt
) {
    public WaitingResponse(final Waiting waiting) {
        this(
                waiting.getId(),
                new ReservationResponse(waiting.getReservation()),
                new MemberResponse(waiting.getMember()),
                waiting.getCreatedAt()
        );
    }
}
