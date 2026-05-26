package roomescape.waiting.infra.projection;

import roomescape.member.Role;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingDetailProjection(
        Long id,
        Long memberId,
        String memberName,
        String memberPassword,
        Role memberRole,
        LocalDate date,
        Long themeId,
        String themeName,
        String themeDescription,
        String thumbnailUrl,
        Long timeId,
        LocalTime startAt,
        Long waitingOrder
) {
}
