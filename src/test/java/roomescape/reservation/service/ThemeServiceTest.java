package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.CurrentDateTime;
import roomescape.fake.TestCurrentDateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.service.dto.ThemeCreateCommand;
import roomescape.reservation.service.dto.ThemeInfo;
import roomescape.fake.FakeReservationDao;
import roomescape.fake.FakeReservationTimeDao;
import roomescape.fake.FakeThemeDao;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ReservationTimeDao;
import roomescape.reservation.repository.ThemeDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    final ThemeDao fakeThemeDao = new FakeThemeDao();
    final ReservationDao fakeReservationDao = new FakeReservationDao();
    final ReservationTimeDao fakeReservationTimeDao = new FakeReservationTimeDao();
    final CurrentDateTime currentDateTime = new TestCurrentDateTime(LocalDateTime.of(2025, 4, 30, 10, 00));
    final ThemeService themeService = new ThemeService(fakeThemeDao, fakeReservationDao, currentDateTime);

    @DisplayName("저장할 테마 이름이 중복될 경우 예외가 발생한다.")
    @Test
    void testValidateNameDuplication() {
        // given
        ThemeCreateCommand request1 = new ThemeCreateCommand("woooteco", "우테코를 탈출하라", "https://www.woowacourse.io/");
        ThemeCreateCommand request2 = new ThemeCreateCommand("woooteco", "우테코에서 살아남기", "https://www.woowacourse2.io/");
        themeService.createTheme(request1);
        // when
        // then
        assertThatThrownBy(() -> themeService.createTheme(request2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 이름의 테마는 이미 존재합니다.");
    }

    @DisplayName("테마를 저장할 수 있다.")
    @Test
    void testCreate() {
        // given
        ThemeCreateCommand request = new ThemeCreateCommand("woooteco", "우테코를 탈출하라", "https://www.woowacourse.io/");
        // when
        ThemeInfo result = themeService.createTheme(request);
        // then
        Theme saved = fakeThemeDao.findById(result.id()).get();
        assertAll(
                () -> assertThat(result.id()).isEqualTo(1L),
                () -> assertThat(result.name()).isEqualTo(request.name()),
                () -> assertThat(result.description()).isEqualTo(request.description()),
                () -> assertThat(result.thumbnail()).isEqualTo(request.thumbnail()),
                () -> assertThat(saved.getName()).isEqualTo(request.name()),
                () -> assertThat(saved.getDescription()).isEqualTo(request.description()),
                () -> assertThat(saved.getThumbnail()).isEqualTo(request.thumbnail())
        );
    }

    @DisplayName("테마 목록을 조회할 수 있다.")
    @Test
    void testFindAll() {
        // given
        ThemeCreateCommand request1 = new ThemeCreateCommand("woooteco1", "우테코를 탈출하라", "https://www.woowacourse.io/");
        ThemeCreateCommand request2 = new ThemeCreateCommand("woooteco2", "우테코에 합격하라", "https://www.woowacourse.io/");
        themeService.createTheme(request1);
        themeService.createTheme(request2);
        // when
        List<ThemeInfo> result = themeService.findAll();
        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("예약이 존재하는 테마를 삭제할 경우 예외가 발생한다.")
    @Test
    void testIllegalDelete() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(11, 0));
        ReservationTime saveTime = fakeReservationTimeDao.save(reservationTime);
        Theme theme = new Theme(null, "우테코방탈출", "탈출탈출탈출", "포비솔라브라운");
        Member member = new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN);
        Theme savedTheme = fakeThemeDao.save(theme);
        fakeReservationDao.save(new Reservation(null, member, LocalDate.now().plusDays(1), saveTime, savedTheme));

        // when
        // then
        assertThatThrownBy(() -> themeService.deleteThemeById(savedTheme.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약이 존재하는 테마는 삭제할 수 없습니다.");
    }

    @DisplayName("테마를 삭제할 수 있다.")
    @Test
    void testDelete() {
        // given
        Theme theme = new Theme(null, "우테코방탈출", "탈출탈출탈출", "포비솔라브라운");
        Theme savedTheme = fakeThemeDao.save(theme);
        // when
        themeService.deleteThemeById(savedTheme.getId());
        // then
        List<Theme> themes = fakeThemeDao.findAll();
        assertThat(themes).isEmpty();
    }
    
    @DisplayName("인기 테마를 조회할 수 있다")
    @Test
    void aa(@Mock ThemeDao themeDao) {
        // given
        LocalDate to = currentDateTime.getDate().minusDays(1);
        LocalDate from = currentDateTime.getDate().minusDays(7);
        Theme theme1 = new Theme(1L, "theme1", "description1", "thumbnail1");
        Theme theme2 = new Theme(2L, "theme2", "description2", "thumbnail2");
        when(themeDao.findPopularThemes(from, to, 10)).thenReturn(List.of(theme1, theme2));
        ThemeService themeService = new ThemeService(themeDao, fakeReservationDao, currentDateTime);
        // when
        List<ThemeInfo> result = themeService.findPopularThemes();
        // then
        assertAll(
                () -> verify(themeDao).findPopularThemes(from, to, 10),
                () -> assertThat(result).hasSize(2)
        );
    }

}
