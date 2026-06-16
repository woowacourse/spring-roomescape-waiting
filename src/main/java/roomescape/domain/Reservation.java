package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.exception.DuplicateEntityException;
import roomescape.exception.EntityNotFoundException;
import roomescape.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Reservation {

    private final Long id;
    private final Theme theme;
    @Getter(AccessLevel.NONE)
    private final Schedule schedule;
    private final ReservationEntries entries;

    private Reservation(Long id, LocalDate date, Theme theme, ReservationTime time, List<ReservationEntry> entries) {
        validateTheme(theme);
        this.id = id;
        this.theme = theme;
        this.schedule = Schedule.of(date, time);
        this.entries = new ReservationEntries(entries);
    }

    public static Reservation restore(Long id, LocalDate date, Theme theme, ReservationTime time,
                                      List<ReservationEntry> entries) {
        return new Reservation(Objects.requireNonNull(id, "복원 시 id 값은 필수입니다"), date, theme, time, entries);
    }

    public static Reservation createSlot(LocalDate date, Theme theme, ReservationTime time) {
        return new Reservation(null, date, theme, time, new ArrayList<>());
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new RoomEscapeException("테마 정보는 비어있을 수 없습니다.");
        }
    }

    public boolean isSameSchedule(LocalDate date, ReservationTime time) {
        return this.schedule.equals(Schedule.of(date, time));
    }

    public void addPendingEntry(String name, Long amount, LocalDateTime now) {
        theme.validatePaymentAmount(amount);
        validateNotPast(now);
        validateDuplicateEntry(name);
        if (entries.hasReservedEntry()) {
            throw new DuplicateEntityException("이미 예약 또는 결제 중인 날짜입니다. (%s %s)", schedule.getDate(), schedule.getStartAt());
        }
        entries.addPending(name, now);
    }

    public ReservationEntry reserve(String name, Long amount, LocalDateTime now) {
        theme.validatePaymentAmount(amount);
        return reserve(name, now);
    }

    public ReservationEntry reserve(String name, LocalDateTime now) {
        validateNotPast(now);
        validateDuplicateEntry(name);
        if (entries.hasReservedEntry()) {
            throw new DuplicateEntityException("이미 예약 된 날짜입니다. (%s %s)", schedule.getDate(), schedule.getStartAt());
        }
        return entries.addReserved(name, now);
    }

    public ReservationEntry reserveOrWait(String name, Long amount, LocalDateTime now) {
        theme.validatePaymentAmount(amount);
        return reserveOrWait(name, now);
    }

    public ReservationEntry reserveOrWait(String name, LocalDateTime now) {
        validateNotPast(now);
        validateDuplicateEntry(name);
        if (entries.hasReservedEntry()) {
            return entries.addWaiting(name, now);
        }
        return entries.addReserved(name, now);
    }

    public List<ReservationEntry> getEntries() {
        return entries.getEntries();
    }

    private void validateNotPast(LocalDateTime now) {
        if (schedule.isPast(now)) {
            throw new RoomEscapeException("이미 지난 예약입니다.");
        }
    }

    private void validateDuplicateEntry(String name) {
        if (entries.hasActiveEntryByName(name)) {
            throw new DuplicateEntityException("이미 예약 또는 대기가 존재합니다. (%s)", name);
        }
    }

    public ReservationEntry findActiveEntry(long entryId) {
        return entries.findById(entryId)
                .filter(ReservationEntry::isActive)
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));
    }

    public ReservationEntry findEntryByNameAndStatus(String name, ReservationStatus status) {
        return entries.findByNameAndStatus(name, status)
                .orElseThrow(() -> new EntityNotFoundException("저장된 예약 엔트리를 찾을 수 없습니다."));
    }

    public void cancelEntry(long entryId) {
        entries.cancel(entryId);

        if (!entries.hasReservedEntry()) {
            entries.promoteFirstWaiting();
        }
    }

    public LocalDate getDate() {
        return schedule.getDate();
    }

    public ReservationTime getTime() {
        return schedule.getTime();
    }
}
