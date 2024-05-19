package roomescape.domain.converter;

import jakarta.persistence.AttributeConverter;
import lombok.Getter;
import roomescape.domain.Role;

@Getter
public class RoleConverter implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role attribute) {
        return attribute.getValue();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        return Role.of(dbData);
    }
}
