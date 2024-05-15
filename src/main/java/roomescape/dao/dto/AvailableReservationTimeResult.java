package roomescape.dao.dto;

public interface AvailableReservationTimeResult {
    boolean getIsBooked();
    long getTimeId();
    String getStartAt();
}
