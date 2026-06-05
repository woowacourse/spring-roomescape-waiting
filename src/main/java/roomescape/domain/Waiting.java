package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.exception.BusinessRuleViolationException;

public class Waiting {
    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final Time time;
    private final Theme theme;
    private final Long storeId;
    private final Long rank;

    public Waiting(Long id, Member member, LocalDate date, Time time, Theme theme, Long storeId, Long rank) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.storeId = storeId;
        this.rank = rank;
    }

    public Waiting(Member member, LocalDate date, Time time, Theme theme, Long storeId) {
        this(null, member, date, time, theme, storeId, null);
    }

    public boolean isOwnedBy(Long memberId) {
        return Objects.equals(this.member.getId(), memberId);
    }

    public Reservation toReservation(LocalDateTime now) {
        if (time.isReservationBefore(now, date)) {
            throw new BusinessRuleViolationException("지난 시간의 예약 대기는 예약으로 전환할 수 없습니다.");
        }
        return Reservation.fromWaiting(this);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getStoreId() {
        return storeId;
    }

    public Long getRank() {
        return rank;
    }
}
