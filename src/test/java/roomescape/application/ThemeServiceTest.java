package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixtures.NORMAL_MEMBER_1;
import static roomescape.TestFixtures.THEME_1;
import static roomescape.TestFixtures.THEME_1_RESULT;
import static roomescape.TestFixtures.THEME_2;
import static roomescape.TestFixtures.THEME_2_RESULT;
import static roomescape.TestFixtures.THEME_3;
import static roomescape.TestFixtures.THEME_3_RESULT;
import static roomescape.TestFixtures.THEME_4;
import static roomescape.TestFixtures.THEME_4_RESULT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.application.reservation.dto.CreateThemeParam;
import roomescape.application.reservation.dto.ThemeResult;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

class ThemeServiceTest extends ServiceIntegrationTest {

    @Test
    void 테마를_전체_조회할_수_있다() {
        //given
        themeRepository.create(THEME_1);
        themeRepository.create(THEME_2);

        //when
        List<ThemeResult> themeResults = themeService.findAll();

        //then
        assertThat(themeResults).isEqualTo(List.of(THEME_1_RESULT, THEME_2_RESULT));
    }

    @Test
    void 테마를_생성할_수_있다() {
        //given
        CreateThemeParam createThemeParam = new CreateThemeParam("test1", "description1", "thumbnail1");

        //when
        Long id = themeService.create(createThemeParam);

        //then
        assertThat(themeRepository.findById(id))
                .hasValue(new Theme(1L, "test1", "description1", "thumbnail1"));
    }

    @Test
    void id값으로_테마를_찾을_수_있다() {
        //given
        themeRepository.create(THEME_1);

        //when
        ThemeResult themeResult = themeService.findById(1L);

        //then
        assertThat(themeResult).isEqualTo(THEME_1_RESULT);
    }

    @Test
    void id값으로_테마를_찾을때_없다면_예외가_발생한다() {
        // given & when & then
        assertThatThrownBy(() -> themeService.findById(1L))
                .isInstanceOf(NotFoundEntityException.class)
                .hasMessage("id에 해당하는 Theme이 없습니다.");
    }

    @Test
    void id값으로_테마를_삭제할_수_있다() {
        //given
        themeRepository.create(THEME_1);

        //when
        themeService.deleteById(1L);

        //then
        assertThat(themeRepository.findById(1L)).isEmpty();
    }

    @Test
    void id값으로_테마를_삭제할떄_예약에서_id가_사용중이라면_예외를_발생시킨다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        themeRepository.create(THEME_1);
        reservationTimeRepository.create(new ReservationTime(1L, LocalTime.of(12, 0)));
        reservationRepository.create(new Reservation(
                NORMAL_MEMBER_1,
                LocalDate.of(2025, 5, 1),
                new ReservationTime(1L, LocalTime.of(12, 0)),
                THEME_1));

        //when & then
        assertThatThrownBy(() -> themeService.deleteById(1L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("해당 테마에 예약이 존재합니다.");
    }

    @Test
    void 최근_일주일간_예약_건수가_많은_테마를_내림차순으로_찾을_수_있다() {
        //given
        insertMember();
        themeRepository.create(THEME_1);
        themeRepository.create(THEME_2);
        themeRepository.create(THEME_3);
        themeRepository.create(THEME_4);

        reservationTimeRepository.create(new ReservationTime(LocalTime.of(12, 0)));

        insertReservation(1L, LocalDate.now(clock).minusDays(1), 1L, 3L);
        insertReservation(1L, LocalDate.now(clock).minusDays(1), 1L, 3L);
        insertReservation(1L, LocalDate.now(clock).minusDays(1), 1L, 3L);
        insertReservation(1L, LocalDate.now(clock).minusDays(1), 1L, 3L);
        insertReservation(1L, LocalDate.now(clock).minusDays(3), 1L, 2L);
        insertReservation(1L, LocalDate.now(clock).minusDays(3), 1L, 2L);
        insertReservation(1L, LocalDate.now(clock).minusDays(4), 1L, 4L);
        insertReservation(1L, LocalDate.now(clock).minusDays(8), 1L, 4L);
        insertReservation(1L, LocalDate.now(clock).minusDays(8), 1L, 4L);
        insertReservation(1L, LocalDate.now(clock).minusDays(8), 1L, 4L);

        //when
        List<ThemeResult> rankBetweenDate = themeService.findRankBetweenDate();

        //then
        assertThat(rankBetweenDate).isEqualTo(List.of(
                THEME_3_RESULT,
                THEME_2_RESULT,
                THEME_4_RESULT,
                THEME_1_RESULT
        ));
    }
}