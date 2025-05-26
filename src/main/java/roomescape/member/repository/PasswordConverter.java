package roomescape.member.repository;

import jakarta.persistence.AttributeConverter;
import roomescape.member.domain.Password;

public class PasswordConverter implements AttributeConverter<Password, String> {

    @Override
    public String convertToDatabaseColumn(Password attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public Password convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return new Password(dbData);
    }
}
