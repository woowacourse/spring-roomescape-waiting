package roomescape.waiting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;

public record WaitingCreateRequest(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        Long themeId,
        Long timeId) {
    public Waiting createWaiting(Reservation reservation, Member waitingMember) {
        return new Waiting(reservation, waitingMember);
    }
}
