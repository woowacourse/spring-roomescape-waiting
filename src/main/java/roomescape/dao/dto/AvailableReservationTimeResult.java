package roomescape.dao.dto;

public record AvailableReservationTimeResult(boolean isBooked, long timeId, String startAt) implements AvailableReservationTimeResultInterface {
    @Override
    public long getTimeId() {
        return timeId;
    }

    @Override
    public boolean getIsBooked() {
        return isBooked;
    }

    @Override
    public String getStartAt() {
        return startAt;
    }
}
