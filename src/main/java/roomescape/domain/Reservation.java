package roomescape.domain;

import java.time.LocalDate;
import roomescape.exception.auth.WrongStoreAccessException;

public class Reservation {

    private final Long id;
    private final Long memberId;
    private final LocalDate date;
    private final ReservationTime time;
    private final Long themeId;
    private final Long storeId;

    public Reservation(Long id, Long memberId, LocalDate date, ReservationTime time, Long themeId, Long storeId) {
        validateMemberId(memberId);
        validateDate(date);
        validateTime(time);
        validateThemeId(themeId);
        validateStoreId(storeId);

        this.id = id;
        this.memberId = memberId;
        this.date = date;
        this.time = time;
        this.themeId = themeId;
        this.storeId = storeId;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public ReservationTime getTime() {
        return time;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getThemeId() {
        return themeId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void validateStoreOwnership(Member member) {
        if (!this.storeId.equals(member.getStoreId())) {
            throw new WrongStoreAccessException();
        }
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 비어 있을 수 없습니다.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 비어 있을 수 없습니다.");
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("예약시간은 비어 있을 수 없습니다.");
        }
    }

    private void validateThemeId(Long themeId) {
        if (themeId == null) {
            throw new IllegalArgumentException("테마 ID는 비어 있을 수 없습니다.");
        }
    }

    private void validateStoreId(Long storeId) {
        if (storeId == null) {
            throw new IllegalArgumentException("매장 ID는 비어 있을 수 없습니다.");
        }
    }
}
