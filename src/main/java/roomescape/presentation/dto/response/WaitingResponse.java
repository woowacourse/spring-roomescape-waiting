package roomescape.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Waiting;

import java.time.LocalDate;
import java.util.List;

public record WaitingResponse(
        Long id,

        MemberResponse member,

        ThemeResponse theme,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        ReservationTimeResponse time
) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                MemberResponse.from(waiting.getMember()),
                ThemeResponse.from(waiting.getReservationInfo().getTheme()),
                waiting.getReservationInfo().getDate(),
                ReservationTimeResponse.from(waiting.getReservationInfo().getTime())
        );
    }

    public static List<WaitingResponse> from(List<Waiting> waitings) {
        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
