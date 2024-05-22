package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.util.Fixture.HORROR_DESCRIPTION;
import static roomescape.util.Fixture.HORROR_THEME_NAME;
import static roomescape.util.Fixture.HOUR_10;
import static roomescape.util.Fixture.HOUR_11;
import static roomescape.util.Fixture.JOJO_EMAIL;
import static roomescape.util.Fixture.JOJO_NAME;
import static roomescape.util.Fixture.JOJO_PASSWORD;
import static roomescape.util.Fixture.KAKI_EMAIL;
import static roomescape.util.Fixture.KAKI_NAME;
import static roomescape.util.Fixture.KAKI_PASSWORD;
import static roomescape.util.Fixture.THUMBNAIL;
import static roomescape.util.Fixture.TODAY;
import static roomescape.util.Fixture.TOMORROW;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.auth.domain.Role;
import roomescape.auth.dto.LoginMember;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationSaveRequest;
import roomescape.reservation.dto.ReservationWaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationService reservationService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @DisplayName("존재하지 않는 예약 시간에 예약을 하면 예외가 발생한다.")
    @Test
    void notExistReservationTimeIdExceptionTest() {
        Theme theme = themeRepository.save(
                new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        LoginMember loginMember = new LoginMember(1L, Role.USER, JOJO_NAME, JOJO_EMAIL);
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(TODAY, theme.getId(), 1L);

        assertThatThrownBy(() -> reservationService.saveReservationSuccess(reservationSaveRequest, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 성공 상태의 중복된 예약이 있다면 예외가 발생한다.")
    @Test
    void validateDuplicatedReservationSuccess() {
        Theme theme = themeRepository.save(
                new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(HOUR_10));

        memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        LoginMember loginMember = new LoginMember(1L, Role.USER, JOJO_NAME, JOJO_EMAIL);
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(TODAY, theme.getId(),
                reservationTime.getId());
        reservationService.saveReservationSuccess(reservationSaveRequest, loginMember);

        ReservationSaveRequest duplicateRequest = new ReservationSaveRequest(TODAY, theme.getId(),
                reservationTime.getId());
        assertThatThrownBy(() -> reservationService.saveReservationSuccess(duplicateRequest, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("동일한 회원이 예약 대기 상태의 중복된 예약을 할 경우 예외가 발생한다.")
    @Test
    void validateDuplicatedReservationWaiting() {
        Theme theme = themeRepository.save(
                new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(HOUR_10));

        memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        LoginMember loginMember = new LoginMember(1L, Role.USER, KAKI_NAME, KAKI_EMAIL);
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(TODAY, theme.getId(),
                reservationTime.getId());
        reservationService.saveReservationWaiting(reservationSaveRequest, loginMember);

        assertThatThrownBy(() -> reservationService.saveReservationWaiting(reservationSaveRequest, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("동일한 회원이 예약 후 해당 예약에 연달아 대기를 걸 경우 예외가 발생한다.")
    @Test
    void validateReservationWaitingAfterReservation() {
        Theme theme = themeRepository.save(
                new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(HOUR_10));

        memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        LoginMember loginMember = new LoginMember(1L, Role.USER, KAKI_NAME, KAKI_EMAIL);
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(TODAY, theme.getId(),
                reservationTime.getId());
        reservationService.saveReservationSuccess(reservationSaveRequest, loginMember);

        assertThatThrownBy(() -> reservationService.saveReservationWaiting(reservationSaveRequest, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("회원 별 예약 목록을 조회 시 대기 상태의 예약은 대기 순서를 함께 반환한다.")
    @Test
    void findMemberReservations() {
        Theme theme = themeRepository.save(
                new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(HOUR_10));

        Member kaki = memberRepository.save(
                Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));
        Member jojo = memberRepository.save(
                Member.createMemberByUserRole(new MemberName(JOJO_NAME), JOJO_EMAIL, JOJO_PASSWORD));

        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(TODAY, theme.getId(),
                reservationTime.getId());

        LoginMember loginMember1 = new LoginMember(kaki.getId(), Role.USER, KAKI_NAME, KAKI_EMAIL);
        reservationService.saveReservationWaiting(reservationSaveRequest, loginMember1);

        LoginMember loginMember2 = new LoginMember(jojo.getId(), Role.USER, JOJO_NAME, JOJO_EMAIL);
        reservationService.saveReservationWaiting(reservationSaveRequest, loginMember2);

        List<MemberReservationResponse> memberReservationResponses = reservationService.findMemberReservations(
                loginMember2);

        assertThat(memberReservationResponses).extracting(MemberReservationResponse::rank)
                .containsExactly(2);
    }

    @DisplayName("현재 날짜 이후의 예약들을 예약 날짜, 예약 시간, 예약 추가 순으로 정렬해 예약 대기 목록을 조회한다.")
    @Test
    void findWaitingReservations() {
        Theme theme = themeRepository.save(
                new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        ReservationTime hour10 = reservationTimeRepository.save(new ReservationTime(HOUR_10));
        ReservationTime hour11 = reservationTimeRepository.save(new ReservationTime(HOUR_11));

        Member kaki = memberRepository.save(
                Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));
        Member jojo = memberRepository.save(
                Member.createMemberByUserRole(new MemberName(JOJO_NAME), JOJO_EMAIL, JOJO_PASSWORD));

        Reservation reservation1 = new Reservation(kaki, TOMORROW, theme, hour11, Status.WAIT);
        Reservation reservation2 = new Reservation(kaki, TODAY, theme, hour10, Status.WAIT);
        Reservation reservation3 = new Reservation(kaki, TODAY, theme, hour11, Status.WAIT);
        Reservation reservation4 = new Reservation(jojo, TOMORROW, theme, hour10, Status.WAIT);

        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        reservationRepository.save(reservation4);

        List<ReservationWaitingResponse> waitingReservations = reservationService.findWaitingReservations();

        assertThat(waitingReservations).extracting(ReservationWaitingResponse::id)
                .containsExactly(reservation2.getId(), reservation3.getId(), reservation4.getId(),
                        reservation1.getId());
    }

    @DisplayName("예약 아이디로 조회 시 존재하지 않는 아이디면 예외가 발생한다.")
    @Test
    void findByIdExceptionTest() {
        assertThatThrownBy(() -> reservationService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("당일 예약을 삭제하면 예외가 발생한다.")
    @Test
    void deleteTodayReservation() {
        Theme theme = themeRepository.save(new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(HOUR_10));

        memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        LoginMember loginMember = new LoginMember(1L, Role.USER, KAKI_NAME, KAKI_EMAIL);
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(TODAY, theme.getId(), reservationTime.getId());
        reservationService.saveReservationSuccess(reservationSaveRequest, loginMember);

        assertThatThrownBy(() -> reservationService.delete(loginMember.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
