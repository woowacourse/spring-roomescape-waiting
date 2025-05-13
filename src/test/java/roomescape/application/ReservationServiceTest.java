package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.testFixture.Fixture.MEMBER1_ADMIN;
import static roomescape.testFixture.Fixture.MEMBER2_USER;
import static roomescape.testFixture.Fixture.MEMBER3_USER;
import static roomescape.testFixture.Fixture.MEMBER4_USER;
import static roomescape.testFixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.application.dto.MemberDto;
import roomescape.application.dto.ReservationDto;
import roomescape.application.dto.TimeDto;
import roomescape.application.dto.UserReservationCreateDto;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.exception.NotFoundException;
import roomescape.testRepository.FakeMemberRepository;
import roomescape.testRepository.FakeReservationRepository;
import roomescape.testRepository.FakeThemeRepository;
import roomescape.testRepository.FakeTimeRepository;

class ReservationServiceTest {

    private ReservationService reservationService;
    private FakeReservationRepository reservationRepository;
    private FakeTimeRepository timeRepository;
    private FakeThemeRepository themeRepository;
    private FakeMemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        timeRepository = new FakeTimeRepository();
        themeRepository = new FakeThemeRepository();
        memberRepository = new FakeMemberRepository();
        TimeService timeService = new TimeService(timeRepository);
        ThemeService themeService = new ThemeService(themeRepository);
        MemberService memberService = new MemberService(memberRepository);
        reservationService = new ReservationService(reservationRepository, timeService, themeService, memberService);
    }

    @DisplayName("예약을 정상적으로 등록한다.")
    @Test
    void register_Reservation() {
        // given
        LocalTime time = LocalTime.of(10, 0);
        timeRepository.save(ReservationTime.withoutId(time));
        themeRepository.save(THEME_1);
        memberRepository.save(MEMBER1_ADMIN);
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;

        UserReservationCreateDto userReservationCreateDto = new UserReservationCreateDto(1L, date, timeId);

        // when
        ReservationDto reservationDto = reservationService.registerReservationByUser(userReservationCreateDto, 1L);

        // then
        assertAll(
                () -> assertThat(reservationDto.id()).isEqualTo(1),
                () -> assertThat(reservationDto.member()).isEqualTo(MemberDto.from(MEMBER1_ADMIN)),
                () -> assertThat(reservationDto.date()).isEqualTo(date),
                () -> assertThat(reservationDto.time()).isEqualTo(new TimeDto(1L, time))
        );
    }

    @DisplayName("존재하지 않는 timeId로 예약 생성 시 예외 발생")
    @Test
    void error_when_timeId_notExist() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 999L;

        UserReservationCreateDto userReservationCreateDto = new UserReservationCreateDto(1L, date, timeId);

        // when & then
        assertThatThrownBy(
                () -> reservationService.registerReservationByUser(userReservationCreateDto, MEMBER1_ADMIN.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("중복된 일시에 예약 생성 시 예외 발생")
    @Test
    void error_when_dateTime_Duplicate() {
        // given
        LocalTime time = LocalTime.of(10, 0);
        timeRepository.save(ReservationTime.withoutId(time));

        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;

        ReservationTime reservationTime = ReservationTime.of(1L, time);
        Reservation reservation = Reservation.of(1L, MEMBER1_ADMIN, THEME_1, date, reservationTime);
        reservationRepository.save(reservation);

        // when
        UserReservationCreateDto userReservationCreateDto = new UserReservationCreateDto(1L, date, timeId);

        // then
        assertThatThrownBy(
                () -> reservationService.registerReservationByUser(userReservationCreateDto, MEMBER2_USER.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("모든 예약을 조회할 수 있다.")
    @Test
    void getAllReservations() {
        // given
        ReservationTime time1 = ReservationTime.of(1L, LocalTime.of(10, 0));
        ReservationTime time2 = ReservationTime.of(2L, LocalTime.of(11, 0));

        reservationRepository.save(Reservation.of(1L, MEMBER2_USER, THEME_1, LocalDate.of(2024, 4, 1), time1));
        reservationRepository.save(Reservation.of(2L, MEMBER3_USER, THEME_1, LocalDate.of(2024, 4, 1), time2));
        reservationRepository.save(Reservation.of(3L, MEMBER4_USER, THEME_1, LocalDate.of(2024, 4, 2), time1));

        // when
        List<ReservationDto> allReservations = reservationService.getAllReservations();

        // then
        assertAll(
                () -> assertThat(allReservations).hasSize(3),
                () -> assertThat(allReservations)
                        .extracting(ReservationDto::member)
                        .containsExactly(MemberDto.from(MEMBER2_USER), MemberDto.from(MEMBER3_USER), MemberDto.from(
                                MEMBER4_USER))
        );
    }

    @DisplayName("예약을 id로 취소할 수 있다.")
    @Test
    void cancelReservation() {
        // given
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        timeRepository.save(time);
        reservationRepository.save(Reservation.of(1L, MEMBER1_ADMIN, THEME_1, LocalDate.of(2024, 4, 1), time));
        assertThat(reservationRepository.findAll()).hasSize(1);

        // when
        reservationService.deleteReservation(1L);

        // then
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @DisplayName("과거 일시로 예약할 수 없다.")
    @Test
    void cannot_register_when_past() {
        // given
        LocalDate date = LocalDate.now().minusDays(1);
        Long timeId = timeRepository.save(ReservationTime.withoutId(LocalTime.of(10, 0)));

        // when
        UserReservationCreateDto request = new UserReservationCreateDto(1L, date, timeId);

        // then
        assertThatThrownBy(() -> reservationService.registerReservationByUser(request, MEMBER1_ADMIN.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("중복된 일시로 예약할 수 없다.")
    @Test
    void cannot_register_when_duplicate() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = timeRepository.save(ReservationTime.withoutId(LocalTime.of(10, 0)));
        ReservationTime reservationTime = ReservationTime.of(timeId, LocalTime.of(10, 0));
        Reservation reservation = Reservation.of(1L, MEMBER1_ADMIN, THEME_1, date, reservationTime);
        reservationRepository.save(reservation);

        // when
        UserReservationCreateDto request = new UserReservationCreateDto(1L, date, timeId);

        // then
        assertThatThrownBy(() -> reservationService.registerReservationByUser(request, MEMBER1_ADMIN.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
