package roomescape.member.repository;

import jakarta.persistence.AttributeConverter;
import roomescape.member.domain.Email;

public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public Email convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return new Email(dbData);
    }
}
