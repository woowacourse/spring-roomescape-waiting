package roomescape.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ServiceTest;
import roomescape.dao.*;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.code.ThemeErrorCode;
import roomescape.exception.domain.ThemeException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class ThemeServiceTest extends ServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private WaitingDao waitingDao;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private SlotDao slotDao;

    @Autowired
    private Clock clock;

    @Test
    void 테마를_생성할_수_있다() {
        // given
        ThemeRequest request = new ThemeRequest(
                "테마1",
                "설명",
                "https://dsf.sdaf"
        );

        // when
        ThemeResponse response = themeService.create(request);

        // then
        assertThat(response)
                .extracting(
                        ThemeResponse::name,
                        ThemeResponse::description,
                        ThemeResponse::thumbnail
                )
                .containsExactly(
                        request.name(),
                        request.description(),
                        request.thumbnail()
                );
    }

    @Test
    void 이름이_같은_테마_생성시_예외가_발생한다() {
        // given
        ThemeRequest request = new ThemeRequest(
                "테마1",
                "설명",
                "https://dsf.sdaf"
        );
        themeService.create(request);

        // when & then
        assertThatThrownBy(() -> themeService.create(request))
                .isInstanceOf(ThemeException.class)
                .hasMessage(ThemeErrorCode.THEME_ALREADY_EXISTS.getMessage());

    }

    @Test
    void 테마를_조회한다() {
        // given
        Theme theme1 = saveTheme("테마1");
        Theme theme2 = saveTheme("테마2");

        // when
        List<ThemeResponse> themes = themeService.getThemes();

        // then
        assertAll(
                () -> assertThat(themes).hasSize(2),
                () -> assertThat(themes)
                        .extracting(
                                ThemeResponse::id,
                                ThemeResponse::name,
                                ThemeResponse::description,
                                ThemeResponse::thumbnail
                        )
                        .containsExactlyInAnyOrder(
                                tuple(
                                        theme1.getId(),
                                        theme1.getName(),
                                        theme1.getDescription(),
                                        theme1.getThumbnail()
                                ),
                                tuple(
                                        theme2.getId(),
                                        theme2.getName(),
                                        theme2.getDescription(),
                                        theme2.getThumbnail()
                                )
                        )
        );
    }

    @Test
    void 인기_테마를_조회한다() {
        // given
        LocalDate fixedToday = LocalDate.now(clock);

        Theme popularTheme = saveTheme("인기 테마");
        Theme normalTheme = saveTheme("보통 테마");
        Theme unpopularTheme = saveTheme("비인기 테마");

        ReservationTime time10 = saveReservationTime(LocalTime.of(10, 0));
        ReservationTime time11 = saveReservationTime(LocalTime.of(11, 0));
        ReservationTime time12 = saveReservationTime(LocalTime.of(12, 0));

        // 인기 테마: 조회 기간 내 예약 3개
        saveReservation("예약자일", fixedToday.minusDays(1), time10, popularTheme);
        saveReservation("예약자이", fixedToday.minusDays(2), time11, popularTheme);
        saveReservation("예약자삼", fixedToday.minusDays(3), time12, popularTheme);

        // 보통 테마: 조회 기간 내 예약 2개
        saveReservation("예약자사", fixedToday.minusDays(1), time10, normalTheme);
        saveReservation("예약자오", fixedToday.minusDays(2), time11, normalTheme);

        // 비인기 테마: 조회 기간 내 예약 1개
        saveReservation("예약자육", fixedToday.minusDays(1), time10, unpopularTheme);

        // 조회 기간 밖 예약: 순위에 반영되면 안 됨
        saveReservation("예약자칠", fixedToday, time10, unpopularTheme);
        saveReservation("예약자팔", fixedToday.minusDays(8), time11, unpopularTheme);

        // when
        List<ThemeResponse> rankings = themeService.getThemeRankings();

        // then
        assertThat(rankings)
                .extracting(ThemeResponse::name)
                .containsExactly(
                        "인기 테마",
                        "보통 테마",
                        "비인기 테마"
                );
    }

    @Test
    void 테마를_삭제할_수_있다() {
        // given
        ThemeResponse response = themeService.create(new ThemeRequest(
                "테마1",
                "설명",
                "https://dsf.sdaf"
        ));
        int beforeSize = themeService.getThemes().size();

        // when
        themeService.delete(response.id());

        // then
        List<ThemeResponse> themes = themeService.getThemes();
        assertAll(
                () -> assertThat(themes).hasSize(beforeSize - 1),
                () -> assertThat(themes)
                        .extracting(ThemeResponse::id)
                        .doesNotContain(response.id())
        );
    }

    @Test
    void 존재하지_않는_테마_삭제시_예외가_발생한다() {
        // given
        long invalidThemeId = 0L;

        // when & then
        assertThatThrownBy(() -> themeService.delete(invalidThemeId))
                .isInstanceOf(ThemeException.class)
                .hasMessage(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    void 테마_삭제시_관련_예약이_존재하면_예외가_발생한다() {
        // given
        Theme theme = saveTheme("테마1");
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(10, 0));

        saveReservation("예약1", LocalDate.of(2026, 5, 8), reservationTime, theme);

        // when & then
        assertThatThrownBy(() -> themeService.delete(theme.getId()))
                .isInstanceOf(ThemeException.class)
                .hasMessage(ThemeErrorCode.THEME_HAS_RESERVATION.getMessage());
    }

    @Test
    void 테마_삭제시_관련_대기가_존재하면_예외가_발생한다() {
        // given
        Theme theme = saveTheme("테마1");
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(10, 0));

        LocalDate reservationDate = LocalDate.of(2026, 5, 8);
        Reservation reservation = saveReservation("예약1", reservationDate, reservationTime, theme);
        saveWaiting(reservationDate, reservationTime, theme, "대기1");
        reservationDao.delete(reservation.getId());

        // when & then
        assertThatThrownBy(() -> themeService.delete(theme.getId()))
                .isInstanceOf(ThemeException.class)
                .hasMessage(ThemeErrorCode.THEME_HAS_WAITING.getMessage());
    }

    private Theme saveTheme(String name) {
        Theme theme = new Theme(
                name,
                "설명",
                "https://dsf.sdaf"
        );
        return themeDao.save(theme);
    }

    private ReservationTime saveReservationTime(LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);
        return reservationTimeDao.save(reservationTime);
    }

    private Reservation saveReservation(
            String name,
            LocalDate date,
            ReservationTime reservationTime,
            Theme theme
    ) {
        Slot savedSlot = slotDao.save(new Slot(date, reservationTime, theme));
        Reservation reservation = new Reservation(
                savedSlot,
                name
        );
        return reservationDao.save(reservation);
    }

    private WaitingRequest saveWaiting(LocalDate date, ReservationTime reservationTime, Theme theme, String name) {
        WaitingRequest request1 = new WaitingRequest(date, reservationTime.getId(), theme.getId(), name);
        waitingService.create(request1);
        return request1;
    }
}
