package roomescape.waiting.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import roomescape.member.domain.Member;
import roomescape.schedule.domain.Schedule;
import roomescape.waiting.domain.Waiting;

import java.time.LocalDate;

public record WaitingCreateRequest(
        @NotNull(message = "예약 날짜는 빈 값이 올 수 없습니다")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull(message = "예약 시간이 올바르지 않습니다")
        Long timeId,
        @NotNull(message = "예약 테마가 올바르지 않습니다")
        Long themeId
) {
    public Waiting toWaiting(Schedule schedule, Member member) {
        return new Waiting(null, schedule, member);
    }
}
