//package roomescape.domain.reservation;
//
//import java.time.LocalDateTime;
//
//import roomescape.common.exception.UnprocessableException;
//import roomescape.domain.theme.Theme;
//
//public class Reservation {
//    private final Long id;
//    private final ReservationName reservationName;
//    private final ReservationDate date;
//    private final ReservationTime time;
//    private final Theme theme;
//    private final Slot1 slot;
//
//    public Reservation(Long id, ReservationName reservationName, ReservationDate date, ReservationTime time, Theme theme, Slot1 slot) {
//        this.id = id;
//        this.reservationName = reservationName;
//        this.date = date;
//        this.time = time;
//        this.theme = theme;
//        this.slot = slot;
//    }
//
//    public static Reservation load(Long id,
//                                   ReservationName reservationName,
//                                   ReservationDate date, ReservationTime time,
//                                   Theme theme,
//                                   Slot1 slot) {
//        return new Reservation(id, reservationName, date, time, theme, slot);
//    }
//
//    public static Reservation create(
//            ReservationName reservationName,
//            ReservationDate date, ReservationTime time,
//            Theme theme,
//            LocalDateTime now,
//            Status status
//    ) {
//        Reservation reservation = new Reservation(null, date, time, theme, Slot1.create(reservationName));
//        reservation.ensureNotPast(now);
//        return reservation;
//    }
//
//    private void ensureNotPast(LocalDateTime now) {
//        LocalDateTime requestDateTime = LocalDateTime.of(date.getDate(), time.getStartAt());
//
//        if (requestDateTime.isBefore(now)) {
//            throw new UnprocessableException("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요");
//        }
//    }
//
//    public boolean isPast(LocalDateTime now) {
//        return LocalDateTime.of(date.getDate(), time.getStartAt()).isBefore(now);
//    }
//
//    public boolean isSameName(String name) {
//        return reservationName.isSame(name);
//    }
//
//    public boolean isApproved() {
//        return status == Status.APPROVED;
//    }
//
//
//    public boolean isSlotChanged(Reservation updated) {
//        return !time.equals(updated.getTime()) || !theme.equals(updated.getTheme()) || !date.equals(updated.getDate());
//    }
//
//    public long getId() {
//        return id;
//    }
//
//    public ReservationName getName() {
//        return reservationName;
//    }
//
//    public ReservationDate getDate() {
//        return date;
//    }
//
//    public ReservationTime getTime() {
//        return time;
//    }
//
//    public Theme getTheme() {
//        return theme;
//    }
//
//    public Status getStatus() {
//        return status;
//    }
//
//    public Integer getRank() {
//        return rank;
//    }
//
//    @Override
//    public final boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Reservation that)) return false;
//        if (id == null || that.id == null) return false;
//        return id.equals(that.id);
//    }
//
//    @Override
//    public int hashCode() {
//        return id != null ? id.hashCode() : System.identityHashCode(this);
//    }
//}
