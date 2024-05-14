package roomescape.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DateUtilTest {

    @Test
    void 십분_후의_시간을_반환() {
        //given
        Date now = new Date();
        Date expected = new Date(now.getTime() + 600000);

        //when
        Date afterTenMinutes = DateUtil.getAfterTenMinutes();

        //then
        long difference = Math.abs(afterTenMinutes.getTime() - expected.getTime());
        assertThat(difference).isLessThan(1000); // 1초(1000밀리초) 이내 차이 허용
    }

    @Test
    void 지나간_날짜와_시간인지_확인() {
        //given
        LocalDate today = LocalDate.now();
        LocalTime beforeOneHour = LocalTime.now().minusHours(1);

        //when
        boolean result = DateUtil.isPastDateTime(today, beforeOneHour);

        //then
        assertThat(result).isTrue();
    }
}
