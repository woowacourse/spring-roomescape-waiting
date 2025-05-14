package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.common.exception.DuplicatedException;
import roomescape.dao.ReservationTimeDao;
import roomescape.dto.request.ReservationRegisterDto;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.fake.FakeMemberDao;
import roomescape.fake.FakeReservationDao;
import roomescape.fake.FakeReservationTimeDao;
import roomescape.fake.FakeThemeDao;
import roomescape.dto.LoginMember;
import roomescape.model.Member;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;

@Import(JdbcTemplate.class)
class ReservationServiceTest {

    private final ReservationService reservationService;
    private final ReservationTimeDao reservationTimeDao;
    private final FakeThemeDao themeDao;
    private final FakeMemberDao memberDao;
    private final LoginMember loginMember;

    public ReservationServiceTest() {
        this.reservationTimeDao = new FakeReservationTimeDao();
        this.themeDao = new FakeThemeDao();
        this.memberDao = new FakeMemberDao();
        this.reservationService = new ReservationService(
                new FakeReservationDao(),
                reservationTimeDao,
                themeDao,
                memberDao
        );
        Long savedId = memberDao.add(new Member("히로", "example@gmail.com", "password", Role.ADMIN));
        this.loginMember = new LoginMember(savedId, "히로", "example@gmail.com", Role.ADMIN);
    }

    @BeforeEach
    void setUp() {
        reservationTimeDao.saveTime(new ReservationTime(LocalTime.of(20, 0)));

        themeDao.saveTheme(new Theme("공포", "무서워요", "image-url"));
        memberDao.add(new Member("히로", "example@gmail.com", "password", Role.ADMIN));
    }

    @DisplayName("예약을 정상적으로 저장한다.")
    @Test
    void test1() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ReservationRegisterDto request = new ReservationRegisterDto(
                tomorrow.toString(),
                1L,
                1L
        );

        // when
        ReservationResponseDto response = reservationService.saveReservation(request, loginMember);

        // then
        assertAll(
                () -> assertThat(response.member().name()).isEqualTo("히로"),
                () -> assertThat(response.date()).isEqualTo(tomorrow)
        );
    }

    @DisplayName("예약을 취소한다")
    @Test
    void test3() {
        // given
        ReservationRegisterDto request = new ReservationRegisterDto(
                LocalDate.now().plusDays(1).toString(), 1L, 1L);
        ReservationResponseDto saved = reservationService.saveReservation(request, loginMember);

        // when
        reservationService.cancelReservation(saved.id());

        // then
        List<ReservationResponseDto> reservations = reservationService.getAllReservations(null);
        assertThat(reservations).isEmpty();
    }

    @DisplayName("이미 존재하는 예약 시간에 예약한다면 예외를 던진다")
    @Test
    void test4() {
        // given
        ReservationRegisterDto request = new ReservationRegisterDto(
                LocalDate.now().plusDays(1).toString(), 1L, 1L);
        reservationService.saveReservation(request, loginMember);
        ReservationRegisterDto savedRequest = new ReservationRegisterDto(
                LocalDate.now().plusDays(1).toString(), 1L, 1L);

        // when && then
        assertThatThrownBy(
                () -> reservationService.saveReservation(savedRequest, loginMember))
                .isInstanceOf(DuplicatedException.class);
    }

    @DisplayName("당일 예약을 한다면 예외를 던진다")
    @Test
    void test5() {
        // given
        ReservationRegisterDto request = new ReservationRegisterDto(
                LocalDate.now().toString(), 1L, 1L);
        // when && then
        assertThatThrownBy(
                () -> reservationService.saveReservation(request, loginMember))
                .isInstanceOf(IllegalStateException.class);
    }
}
