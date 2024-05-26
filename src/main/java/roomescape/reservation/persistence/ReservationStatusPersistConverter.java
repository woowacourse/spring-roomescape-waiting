package roomescape.reservation.persistence;

import jakarta.persistence.AttributeConverter;
import org.springframework.stereotype.Component;
import roomescape.global.exception.ViolationException;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.exception.DataNotConvertedException;

@Component
public class ReservationStatusPersistConverter implements AttributeConverter<ReservationStatus, String> {
    @Override
    public String convertToDatabaseColumn(ReservationStatus attribute) {
        if (attribute == null) {
            throw new DataNotConvertedException("ReservationStatus가 null이어서 db에 저장할 수 없습니다.");
        }
        return attribute.getIdentifier();
    }

    @Override
    public ReservationStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            throw new DataNotConvertedException("ReservationStatus의 dbData가 비어있습니다.");
        }
        try {
            return ReservationStatus.from(dbData);
        } catch (ViolationException e) {
            throw new DataNotConvertedException("ReservationStatus의 dbData가 유효하지 않습니다.");
        }
    }
}
