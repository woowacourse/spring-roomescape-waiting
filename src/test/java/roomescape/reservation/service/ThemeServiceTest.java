package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.ThemeRequest;
import roomescape.reservation.dto.ThemeResponse;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ReservationTimeDao;
import roomescape.reservation.repository.ThemeDao;

@ActiveProfiles("test")
@JdbcTest
@Import({ReservationDao.class, ReservationTimeDao.class, ThemeDao.class, MemberDao.class,
        ThemeService.class})
class ThemeServiceTest {

    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ReservationTimeDao reservationTimeRepository;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private ThemeService themeService;

    @DisplayName("모든 테마 정보를 가져온다")
    @Test
    void test1() {
        // given
        themeDao.save(Theme.withoutId("테마1", "테마1", "www.m.com"));
        themeDao.save(Theme.withoutId("테마2", "테마2", "www.m.com"));

        // when
        List<ThemeResponse> responses = themeService.getAll();

        // then
        assertThat(responses).hasSize(2);
    }

    @DisplayName("테마가 없다면 빈 테마 정보를 가져온다")
    @Test
    void test2() {
        // when
        List<ThemeResponse> responses = themeService.getAll();

        // then
        assertThat(responses).isEmpty();
    }

    @DisplayName("테마를 추가한다")
    @Test
    void test3() {
        // given
        ThemeRequest request = new ThemeRequest("테마1", "테마1", "www.m.com");

        // when
        ThemeResponse response = themeService.create(request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("테마1");
        assertThat(response.description()).isEqualTo("테마1");
        assertThat(response.thumbnail()).isEqualTo("www.m.com");
    }

    @DisplayName("테마를 삭제한다")
    @Test
    void test4() {
        // given
        ThemeRequest request = new ThemeRequest("테마1", "테마1", "www.m.com");
        ThemeResponse response = themeService.create(request);
        Long themeId = response.id();

        // when
        themeService.delete(themeId);

        // then
        assertThat(themeDao.findAll()).isEmpty();
    }

    @DisplayName("없는 테마를 삭제할 수 없다")
    @Test
    void test5() {
        // when & then
        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("예약 정보가 있는 테마의 경우 삭제할 수 없다")
    @Test
    void test6() {
        // given
        Theme savedTheme = themeDao.save(Theme.withoutId("포스티", "공포", "wwww.um.com"));
        Long themeId = savedTheme.getId();

        LocalTime time = LocalTime.of(8, 0);
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.withoutId(time));

        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberDao.save(member);

        LocalDate date = LocalDate.of(2024, 4, 29);
        reservationDao.save(Reservation.withoutId(savedMember, date, savedTime, savedTheme));

        // when & then
        assertThatThrownBy(() -> themeService.delete(themeId))
                .isInstanceOf(AlreadyInUseException.class);
    }

    @DisplayName("1~8일 전 사이의 예약이 많은 최대 10개의 테마 정보를 가져온다")
    @Test
    void test7() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(10, 0)));
        LocalDate date = LocalDateTime.now().toLocalDate();
        Member member = memberDao.save(new Member("포스티", "test@test.com", "12341234", Role.MEMBER));

        Theme theme1 = themeDao.save(Theme.withoutId("테마1", "테마1", "www.m.com"));
        Theme theme2 = themeDao.save(Theme.withoutId("테마2", "테마2", "www.m.com"));
        Theme theme3 = themeDao.save(Theme.withoutId("테마3", "테마3", "www.m.com"));
        reservationDao.save(Reservation.withoutId(member, date.minusDays(1), reservationTime, theme1));
        reservationDao.save(Reservation.withoutId(member, date.minusDays(2), reservationTime, theme1));
        reservationDao.save(Reservation.withoutId(member, date.minusDays(3), reservationTime, theme1));
        reservationDao.save(Reservation.withoutId(member, date.minusDays(4), reservationTime, theme2));
        reservationDao.save(Reservation.withoutId(member, date.minusDays(5), reservationTime, theme2));
        reservationDao.save(Reservation.withoutId(member, date.minusDays(6), reservationTime, theme3));

        // when
        List<ThemeResponse> responses = themeService.getPopularThemes();

        // then
        assertThat(responses).containsExactly(
                ThemeResponse.from(theme1),
                ThemeResponse.from(theme2),
                ThemeResponse.from(theme3)
        );
    }
}
