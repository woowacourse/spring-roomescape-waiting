package roomescape.domain;

import roomescape.exception.auth.WrongStoreAccessException;
import roomescape.util.Validator;

import java.time.LocalDate;

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
        Validator.notNull(memberId, "회원 ID는 비어 있을 수 없습니다.");
        Validator.positive(memberId, "회원 ID는 양수여야 합니다.");
    }

    private void validateDate(LocalDate date) {
        Validator.notNull(date, "날짜는 비어 있을 수 없습니다.");
    }

    private void validateTime(ReservationTime time) {
        Validator.notNull(time, "예약시간은 비어 있을 수 없습니다.");
    }

    private void validateThemeId(Long themeId) {
        Validator.notNull(themeId, "테마 ID는 비어 있을 수 없습니다.");
        Validator.positive(themeId, "테마 ID는 양수여야 합니다.");
    }

    private void validateStoreId(Long storeId) {
        Validator.notNull(storeId, "매장 ID는 비어 있을 수 없습니다.");
        Validator.positive(storeId, "매장 ID는 양수여야 합니다.");
    }

    public boolean isReservedBy(long memberId) {
        return this.memberId.equals(memberId);
    }

    public boolean isPast() {
        return time.isPast(date);
    }
}
