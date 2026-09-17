package roomescape.domain.reservationStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ReservationStatusConverter implements AttributeConverter<ReservationStatus, String> {

    @Override
    public String convertToDatabaseColumn(ReservationStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getName();
    }

    @Override
    public ReservationStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return switch (dbData) {
            case "PENDING" -> PendingStatus.getInstance();
            case "CONFIRMED" -> ConfirmedStatus.getInstance();
            case "COMPLETED" -> CompletedStatus.getInstance();
            case "CANCELLED" -> CancelledStatus.getInstance();
            default -> throw new IllegalArgumentException("존재하지 않는 예약 상태입니다.");
        };
    }
}
