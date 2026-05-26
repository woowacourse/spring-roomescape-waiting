package roomescape.repository;

import common.exception.RoomEscapeException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;


import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@JdbcTest
@Import(value = {
        ReservationRepository.class,
        ReservationTimeRepository.class,
        ThemeRepository.class
})
class ReservationRepositoryTest {
    private final static Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-10T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 10);

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private ReservationTime giveTime(int hour){
        return timeRepository.save(ReservationTime.of(LocalTime.of(hour, 0)));
    }

    private Theme giveTheme(String name){
        return themeRepository.save(Theme.create(new ThemeName(name), name + "테마에 관한 설명 입니다.", new ThumbnailUrl("https://test-theme.com")));
    }

    private Reservation reservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        return Reservation.reserve(new ReservationName(name), new ReservationDate(date), time, theme, LocalDateTime.now(FIXED_CLOCK));
    }

    @Nested
    @DisplayName("")
    class Exists{

        @Test
        void 예약을_할_때_같은_슬롯이면_true() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(14);

            String name = "달수";
            Reservation saved = reservationRepository.save(reservation(name, TODAY, time, theme));

            assertThat(reservationRepository.existsByTimeAndThemeAndDateAndName(time.getId(), theme.getId(), TODAY, name)).isTrue();
        }
        @Test
        void 예약을_할_때_슬롯의_이름_날짜_시간_테마가_하나라도_다르면_false() {
            Theme theme1 = giveTheme("테마1");
            Theme theme2 = giveTheme("테마2");
            ReservationTime time1 = giveTime(14);
            ReservationTime time2 = giveTime(15);

            String name = "달수";
            reservationRepository.save(reservation(name, TODAY, time1, theme1));

            assertSoftly(soft -> {
                soft.assertThat(reservationRepository.existsByTimeAndThemeAndDateAndName(time1.getId(), theme1.getId(), TODAY, "other")).isFalse();
                soft.assertThat(reservationRepository.existsByTimeAndThemeAndDateAndName(time1.getId(), theme2.getId(), TODAY, name)).isFalse();
                soft.assertThat(reservationRepository.existsByTimeAndThemeAndDateAndName(time2.getId(), theme1.getId(), TODAY, name)).isFalse();
                soft.assertThat(reservationRepository.existsByTimeAndThemeAndDateAndName(time1.getId(), theme1.getId(), TODAY.plusDays(1), name)).isFalse();
            });
        }
    }
}
