package roomescape.service.dto.output;

import java.util.List;
import roomescape.domain.reservation.Waiting;

public record WaitingOutput(long id, ThemeOutput theme, String date, ReservationTimeOutput time,
                            MemberOutput member) {
    public static WaitingOutput toOutput(final Waiting waiting) {
        return new WaitingOutput(
                waiting.getId(),
                ThemeOutput.toOutput(waiting.getTheme()),
                waiting.getDate().asString(),
                ReservationTimeOutput.toOutput(waiting.getTime()),
                MemberOutput.toOutput(waiting.getMember())
        );
    }

    public static List<WaitingOutput> toOutputs(List<Waiting> waitings) {
        return waitings.stream()
                .map(WaitingOutput::toOutput)
                .toList();
    }
}
