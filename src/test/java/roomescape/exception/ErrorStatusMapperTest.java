package roomescape.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorCode;
import roomescape.common.exception.ErrorStatusMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorStatusMapperTest {

    private final ErrorStatusMapper errorStatusMapper = new ErrorStatusMapper();

    @Test
    void 예약_검증_예외를_bad_request로_매핑한다() {
        assertThat(errorStatusMapper.map(ErrorCode.INVALID_RESERVATION_ID)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.INVALID_RESERVATION_NAME)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.INVALID_RESERVATION_DATE)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.INVALID_RESERVATION_TIME)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.INVALID_RESERVATION_TIME_ID)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.RESERVATION_ALREADY_HAS_ID)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.RESERVATION_ALREADY_EXISTS)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.RESERVATION_TIME_ALREADY_HAS_ID)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.RESERVATION_TIME_ALREADY_EXISTS)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.INVALID_THEME_ID)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.INVALID_THEME_NAME)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.INVALID_THEME_DESCRIPTION)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.INVALID_THEME_THUMBNAIL)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.INVALID_THEME)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.THEME_ALREADY_HAS_ID)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorStatusMapper.map(ErrorCode.RESERVATION_NOT_FOUND)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(errorStatusMapper.map(ErrorCode.RESERVATION_TIME_NOT_FOUND)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(errorStatusMapper.map(ErrorCode.THEME_NOT_FOUND)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(errorStatusMapper.map(ErrorCode.RESERVATION_CREATE_FAILED)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(errorStatusMapper.map(ErrorCode.RESERVATION_TIME_CREATE_FAILED)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(errorStatusMapper.map(ErrorCode.THEME_CREATE_FAILED)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
