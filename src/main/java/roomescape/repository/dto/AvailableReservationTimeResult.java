package roomescape.repository.dto;

public interface AvailableReservationTimeResult {
    boolean getIsBooked();

    long getTimeId();

    String getStartAt();
}
