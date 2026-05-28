package roomescape.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private LocalDate date;
    private ReservationTime time;
    private ReservationEntries entries;

    public Reservation(Long id, LocalDate date, Theme theme, ReservationTime time, List<ReservationEntry> entries) {
        validateReservation(date, theme, time);
        this.id = id;
        this.date = date;
        this.theme = theme;
        this.time = time;
        this.entries = new ReservationEntries(entries);
    }

    public static Reservation createSlot(LocalDate date, Theme theme, ReservationTime time) {
        Reservation reservation = new Reservation(null, date, theme, time, new ArrayList<>());
        validatePastDateTime(date, time);
        return reservation;
    }

    private static void validatePastDateTime(LocalDate date, ReservationTime time) {
        if (time.isPast(date)) {
            throw new RoomEscapeException("이전 날짜로 예약 할 수 없습니다.");
        }
    }

    private static void validateReservation(LocalDate date, Theme theme, ReservationTime time) {
        validateTheme(theme);
        validateReservationDateTime(date, time);
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new RoomEscapeException("테마 정보는 비어있을 수 없습니다.");
        }
    }

    private static void validateReservationDateTime(LocalDate date, ReservationTime time) {
        if (date == null || time == null) {
            throw new RoomEscapeException("예약 날짜 및 시간 정보는 비어있을 수 없습니다.");
        }
    }

    public boolean isSameSlot(LocalDate date, ReservationTime time) {
        return this.date.isEqual(date) && this.time.equals(time);
    }

    public ReservationEntry reserve(String name) {
        validateNotPast();
        validateDuplicateEntry(name);
        if (entries.hasReservedEntry()) {
            throw new DuplicateEntityException("이미 예약 된 날짜입니다. (%s %s)", date, time.getStartAt());
        }
        return entries.addReserved(name, this);
    }

    public ReservationEntry joinWaitingList(String name) {
        validateNotPast();
        validateDuplicateEntry(name);
        if (entries.hasReservedEntry()) {
            return entries.addWaiting(name, this);
        }
        return entries.addReserved(name, this);
    }

    public List<ReservationEntry> getEntries() {
        return entries.getEntries();
    }

    private void validateNotPast() {
        if (this.time.isPast(this.date)) {
            throw new RoomEscapeException("이미 지난 예약입니다.");
        }
    }

    private void validateDuplicateEntry(String name) {
        if (entries.hasActiveEntryByName(name)) {
            throw new DuplicateEntityException("이미 예약 또는 대기가 존재합니다. (%s)", name);
        }
    }

    public ReservationEntry findReservedEntry(long entryId) {
        return entries.findById(entryId)
                .filter(ReservationEntry::isReserved)
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));
    }

    public ReservationEntry findEntryByNameAndStatus(String name, ReservationStatus status) {
        return entries.findByNameAndStatus(name, status)
                .orElseThrow(() -> new EntityNotFoundException("저장된 예약 엔트리를 찾을 수 없습니다."));
    }

    public void cancelEntry(long entryId) {
        ReservationEntry entry = entries.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));

        boolean wasReserved = entry.isReserved();
        entry.cancel();

        if (wasReserved) {
            entries.promoteFirstWaiting();
        }
    }
}
