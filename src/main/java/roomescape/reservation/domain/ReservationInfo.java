//package roomescape.reservation.domain;
//
//import jakarta.persistence.Embeddable;
//import java.time.LocalDate;
//
//@Embeddable
//public class ReservationInfo {
//
//    private final Long memberId;
//    private final LocalDate date;
//    private final Long timeId;
//    private final Long themeId;
//
//    protected ReservationInfo() {
//        this.memberId = null;
//        this.date = null;
//        this.timeId = null;
//        this.themeId = null;
//    }
//
//    public ReservationInfo(Long memberId, LocalDate date, Long timeId, Long themeId) {
//        this.memberId = memberId;
//        this.date = date;
//        this.timeId = timeId;
//        this.themeId = themeId;
//    }
//
//    public Long getMemberId() { return memberId; }
//    public LocalDate getDate() { return date; }
//    public Long getTimeId() { return timeId; }
//    public Long getThemeId() { return themeId; }
//}
