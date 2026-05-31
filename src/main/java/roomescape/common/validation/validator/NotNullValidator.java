package roomescape.common.validation.validator;

import roomescape.common.validation.annotation.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class NotNullValidator {

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
            validateNotNull(field, value, errors);
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

    private void validateNotNull(Field field, Object value, List<String> errors) {
        NotNull annotation = field.getAnnotation(NotNull.class);
        if (annotation == null) {
            return;
        }

        if (value == null) {
            errors.add(annotation.message());
        }
    }

}
