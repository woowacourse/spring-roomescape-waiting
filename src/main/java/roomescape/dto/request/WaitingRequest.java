package roomescape.dto.request;

import roomescape.domain.*;

import java.time.LocalDate;

public record WaitingRequest(Long memberId, LocalDate date, Long timeId, Long themeId) {

    public WaitingRequest{
        isValid(memberId, date, timeId, themeId);
    }

    public static WaitingRequest from(Long memberId, MemberWaitingRequest memberWaitingRequest) {
        return new WaitingRequest(memberId, memberWaitingRequest.date(),
                memberWaitingRequest.timeId(), memberWaitingRequest.themeId());
    }

    public Waiting toEntity(Member member, TimeSlot timeSlot, Theme theme) {
        return new Waiting(null, member, date, timeSlot, theme, ReservationStatus.WAITING);
    }

    private void isValid(Long memberId, LocalDate date, Long timeId, Long themeId) {
        if (memberId == null) {
            throw new IllegalArgumentException("[ERROR] 예약자는 비워둘 수 없습니다.");
        }

        if (date == null || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("[ERROR] 올바르지 않은 예약 날짜입니다.");
        }

        if (timeId == null) {
            throw new IllegalArgumentException("[ERROR] 올바르지 않은 예약 시간입니다.");
        }

        if (themeId == null) {
            throw new IllegalArgumentException("[ERROR] 올바르지 않은 테마 입니다.");
        }
    }
}
