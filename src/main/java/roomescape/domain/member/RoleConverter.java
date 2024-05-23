package roomescape.domain.member;

import jakarta.persistence.AttributeConverter;
import lombok.Getter;

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
