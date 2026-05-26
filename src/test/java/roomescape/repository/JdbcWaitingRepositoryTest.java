package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@JdbcTest
@Import({
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class,
        JdbcWaitingRepository.class
})
class JdbcWaitingRepositoryTest {

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    WaitingRepository waitingRepository;

    @Test
    void 대기를_등록하면_id를_부여한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        Waiting waiting = Waiting.create(
                "루드비코", LocalDate.parse("2026-05-06"),
                reservationTime,
                theme,
                1L
        );

        // when
        Waiting saved = waitingRepository.save(waiting);

        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 대기를_id로_조회한다() {
    }

    @Test
    void 대기목록을_이름으로_조회한다() {
    }

    @Test
    void 대기를_날짜_시간_테마로_조회한다() {
    }

    @Test
    void 대기를_대기_id로_삭제한다() {
    }

}
