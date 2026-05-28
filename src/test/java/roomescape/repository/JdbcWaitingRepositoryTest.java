package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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
        Optional<Waiting> result = waitingRepository.findById(saved.getId());

        // then
        assertThat(result)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    void 대기목록을_이름으로_조회한다() {
        // given
        String name = "루드비코";
        ReservationTime reservationTime1 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        ReservationTime reservationTime2 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("11:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));

        Waiting waiting1 = waitingRepository.save(Waiting.create(
                name, LocalDate.parse("2026-05-06"), reservationTime1, theme, 1L));
        Waiting waiting2 = waitingRepository.save(Waiting.create(
                name, LocalDate.parse("2026-05-07"), reservationTime2, theme, 1L));
        waitingRepository.save(Waiting.create(
                "코코", LocalDate.parse("2026-05-06"), reservationTime1, theme, 2L));

        // when
        List<Waiting> result = waitingRepository.findByName(name);

        // then
        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(waiting1, waiting2);
    }

    @Test
    void 대기를_대기_id로_삭제한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        Waiting saved = waitingRepository.save(Waiting.create(
                "루드비코", LocalDate.parse("2026-05-06"), reservationTime, theme, 1L));

        // when
        waitingRepository.delete(saved.getId());
        Optional<Waiting> result = waitingRepository.findById(saved.getId());

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    void 이름_날짜_시간_테마가_모두_일치하는_대기가_이미_존재하는지_확인한다() {
        // given
        String name = "루드비코";
        LocalDate date = LocalDate.parse("2026-05-06");
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        Waiting waiting = Waiting.create(
                name,
                date,
                reservationTime,
                theme,
                1L
        );
        waitingRepository.save(waiting);

        // when
        boolean exists = waitingRepository.existsByNameAndDateAndTimeAndTheme(
                name,
                date,
                reservationTime,
                theme
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 대기가_존재하지_않으면_false를_반환한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));

        // when
        boolean exists = waitingRepository.existsByNameAndDateAndTimeAndTheme(
                "루드비코",
                LocalDate.parse("2026-05-06"),
                reservationTime,
                theme
        );

        // then
        assertThat(exists).isFalse();
    }
}
