package roomescape.name.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.vo.Name;
import roomescape.exception.model.RoomEscapeException;
import roomescape.exception.global.NameExceptionCode;

class NameTest {

    @Test
    @DisplayName("이름에 특수문자가 들어가는 경우 에러를 발생한다.")
    void validation_ShouldThrowException_WhenNameContainsSymbol() {
        Throwable illegalNameFormat = assertThrows(RoomEscapeException.class,
                () -> new Name("@특수문자"));

        assertEquals(NameExceptionCode.ILLEGAL_NAME_FORM_EXCEPTION.getMessage(),
                illegalNameFormat.getMessage());
    }

    @Test
    @DisplayName("이름이 Null 인 경우 에러를 발생한다.")
    void validation_ShouldThrowException_WhenNameIsNull() {
        Throwable nameIsNull = assertThrows(RoomEscapeException.class,
                () -> new Name(null));
        assertEquals(NameExceptionCode.NAME_IS_NULL_OR_BLANK_EXCEPTION.getMessage(), nameIsNull.getMessage());
    }

    @Test
    @DisplayName("이름이 빈값인 경우 에러를 발생한다.")
    void validation_ShouldThrowException_WhenNameIsEmpty() {
        Throwable nameIsEmpty = assertThrows(
                RoomEscapeException.class, () -> new Name(" "));

        assertEquals(NameExceptionCode.NAME_IS_NULL_OR_BLANK_EXCEPTION.getMessage(), nameIsEmpty.getMessage());
    }
}
