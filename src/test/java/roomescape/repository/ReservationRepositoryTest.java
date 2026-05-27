package roomescape.repository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


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
    private static final LocalDate FUTURE = LocalDate.of(2099, 1, 1);

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
    @DisplayName("save")
    class Save {

        @Test
        void 예약을_저장하면_ID가_부여된_예약이_반환된다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            Reservation saved = reservationRepository.save(reservation("달수", FUTURE, time, theme));

            assertSoftly(soft -> {
                soft.assertThat(saved.getId()).isPositive();
                soft.assertThat(saved.getName().getValue()).isEqualTo("달수");
                soft.assertThat(saved.getDate().getDate()).isEqualTo(FUTURE);
            });
        }

        @Test
        void 여러_예약을_저장하면_각기_다른_ID가_부여된다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            Reservation first = reservationRepository.save(reservation("달수", FUTURE, time, theme));
            Reservation second = reservationRepository.save(reservation("민구", FUTURE.plusDays(1), time, theme));

            assertThat(first.getId()).isNotEqualTo(second.getId());
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        void 예약이_없으면_빈_목록을_반환한다() {
            assertThat(reservationRepository.findAll()).isEmpty();
        }

        @Test
        void 저장된_예약을_모두_반환한다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.save(reservation("민구", FUTURE.plusDays(1), time, theme));

            assertThat(reservationRepository.findAll()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findAllByName")
    class FindAllByName {

        @Test
        void 이름으로_조회하면_해당_이름의_예약만_반환된다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.save(reservation("달수", FUTURE.plusDays(1), time, theme));
            reservationRepository.save(reservation("민구", FUTURE.plusDays(2), time, theme));

            assertThat(reservationRepository.findAllByName("달수")).hasSize(2);
        }

        @Test
        void 존재하지_않는_이름으로_조회하면_빈_목록을_반환한다() {
            assertThat(reservationRepository.findAllByName("없는이름")).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        void ID로_조회하면_해당_예약이_반환된다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            Reservation saved = reservationRepository.save(reservation("달수", FUTURE, time, theme));

            assertThat(reservationRepository.findById(saved.getId())).isPresent();
        }

        @Test
        void 존재하지_않는_ID로_조회하면_빈_Optional을_반환한다() {
            assertThat(reservationRepository.findById(Long.MAX_VALUE)).isEmpty();
        }

        @Test
        void 조회한_예약의_필드가_저장된_값과_일치한다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            Reservation saved = reservationRepository.save(reservation("달수", FUTURE, time, theme));
            Reservation found = reservationRepository.findById(saved.getId()).orElseThrow();

            assertSoftly(soft -> {
                soft.assertThat(found.getName().getValue()).isEqualTo("달수");
                soft.assertThat(found.getDate().getDate()).isEqualTo(FUTURE);
                soft.assertThat(found.getTime().getId()).isEqualTo(time.getId());
                soft.assertThat(found.getTheme().getId()).isEqualTo(theme.getId());
            });
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        void 예약을_수정하면_변경된_내용이_반영된다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time1 = giveTime(10);
            ReservationTime time2 = giveTime(14);

            Reservation saved = reservationRepository.save(reservation("달수", FUTURE, time1, theme));
            Reservation target = reservation("민구", FUTURE.plusDays(1), time2, theme);

            Reservation updated = reservationRepository.update(saved.getId(), target);

            assertSoftly(soft -> {
                soft.assertThat(updated.getId()).isEqualTo(saved.getId());
                soft.assertThat(updated.getName().getValue()).isEqualTo("민구");
                soft.assertThat(updated.getDate().getDate()).isEqualTo(FUTURE.plusDays(1));
                soft.assertThat(updated.getTime().getId()).isEqualTo(time2.getId());
            });
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        void 예약을_삭제하면_조회할_수_없다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            Reservation saved = reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.deleteById(saved.getId());

            assertThat(reservationRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        void 예약을_삭제하면_전체_목록에서도_제외된다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            Reservation r1 = reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.save(reservation("민구", FUTURE.plusDays(1), time, theme));

            reservationRepository.deleteById(r1.getId());

            assertThat(reservationRepository.findAll()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findByTimeAndThemeAndDate")
    class FindByTimeAndThemeAndDate {

        @Test
        void 같은_시간_테마_날짜의_예약을_모두_반환한다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.save(reservation("민구", FUTURE, time, theme));

            assertThat(reservationRepository.findByTimeAndThemeAndDate(time, theme,
                    new ReservationDate(FUTURE))).hasSize(2);
        }

        @Test
        void 다른_날짜의_예약은_포함되지_않는다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.save(reservation("민구", FUTURE.plusDays(1), time, theme));

            assertThat(reservationRepository.findByTimeAndThemeAndDate(time, theme,
                    new ReservationDate(FUTURE))).hasSize(1);
        }
    }

    @Nested
    @DisplayName("existsByTimeId / existsByThemeId")
    class ExistsByFk {

        @Test
        void 해당_시간으로_예약이_있으면_true를_반환한다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));

            assertThat(reservationRepository.existsByTimeId(time.getId())).isTrue();
        }

        @Test
        void 해당_시간으로_예약이_없으면_false를_반환한다() {
            ReservationTime time = giveTime(10);

            assertThat(reservationRepository.existsByTimeId(time.getId())).isFalse();
        }

        @Test
        void 해당_테마로_예약이_있으면_true를_반환한다() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));

            assertThat(reservationRepository.existsByThemeId(theme.getId())).isTrue();
        }

        @Test
        void 해당_테마로_예약이_없으면_false를_반환한다() {
            Theme theme = giveTheme("테마1");

            assertThat(reservationRepository.existsByThemeId(theme.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByTimeAndThemeAndDateAndName")
    class Exists {

        @Test
        void 예약을_할_때_같은_슬롯이면_true() {
            Theme theme = giveTheme("테마1");
            ReservationTime time = giveTime(14);

            String name = "달수";
            reservationRepository.save(reservation(name, TODAY, time, theme));

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
