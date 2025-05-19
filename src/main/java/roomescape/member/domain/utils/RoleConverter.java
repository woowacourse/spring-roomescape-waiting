package roomescape.member.domain.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import roomescape.member.domain.Role;

@Converter
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role attribute) {
        return attribute.getCode();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        return Role.ofCode(dbData);
    }
}
