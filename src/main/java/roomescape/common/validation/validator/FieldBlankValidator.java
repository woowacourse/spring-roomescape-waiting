package roomescape.common.validation.validator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import roomescape.common.validation.annotation.NotBlank;

public class FieldBlankValidator {

    public List<String> validate(Object target) {
        List<String> errors = new ArrayList<>();
        if (target == null) {
            errors.add("타겟 객체가 null일 수 없습니다.");
            return errors;
        }

        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = getFieldValue(field, target);
            validateNotBlank(field, value, errors);
            if (!errors.isEmpty()) {
                return errors;
            }
        }

        return errors;
    }

    private Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("필드에 접근할 수 없습니다: " + field.getName(), e);
        }
    }

    private void validateNotBlank(Field field, Object value, List<String> errors) {
        NotBlank annotation = field.getAnnotation(NotBlank.class);
        if (annotation == null) {
            return;
        }

        if (value == null) {
            errors.add(field.getName() + "은 Null일 수 없습니다.");
            return;
        }

        if (!(value instanceof String)) {
            errors.add("@NotBlank는 오직 String 타입에만 사용할 수 있습니다.");
            return;
        }

        String stringValue = (String) value;
        if (stringValue.isBlank()) {
            errors.add(annotation.message());
        }
    }

}
