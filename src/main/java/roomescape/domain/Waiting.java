package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
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
        return promoteToReservation(now)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "지난 시간의 예약 대기는 예약으로 전환할 수 없습니다."));
    }

    public Optional<Reservation> promoteToReservation(LocalDateTime now) {
        if (isPast(now)) {
            return Optional.empty();
        }
        return Optional.of(Reservation.from(member, getSlot()));
    }

    private boolean isPast(LocalDateTime now) {
        return time.isReservationBefore(now, date);
    }

    public Slot getSlot() {
        return new Slot(date, time, theme, storeId);
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
