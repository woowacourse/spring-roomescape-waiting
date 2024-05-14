package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.getMemberChoco;
import static roomescape.fixture.MemberFixture.getMemberClover;
import static roomescape.fixture.ReservationFixture.getNextDayReservation;
import static roomescape.fixture.ReservationTimeFixture.getNoon;
import static roomescape.fixture.ThemeFixture.getTheme1;
import static roomescape.fixture.ThemeFixture.getTheme2;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberSignUp;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.controller.dto.ReservationQueryRequest;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.dao.FakeMemberDao;
import roomescape.reservation.dao.FakeMemberReservationDao;
import roomescape.reservation.dao.FakeReservationDao;
import roomescape.reservation.dao.FakeReservationTimeDao;
import roomescape.reservation.dao.FakeThemeDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;

@DisplayName("예약 로직 테스트")
class ReservationServiceTest {
    ReservationRepository reservationRepository;
    ReservationTimeRepository reservationTimeRepository;
    ThemeRepository themeRepository;
    MemberRepository memberRepository;
    MemberReservationRepository memberReservationRepository;
    ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationDao();
        reservationTimeRepository = new FakeReservationTimeDao(reservationRepository);
        memberReservationRepository = new FakeMemberReservationDao();
        themeRepository = new FakeThemeDao(memberReservationRepository);
        memberRepository = new FakeMemberDao();
        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository,
                memberRepository, memberReservationRepository);
    }

    @DisplayName("예약 생성에 성공한다.")
    @Test
    void create() {
        //given
        Member member = memberRepository.save(
                new MemberSignUp(getMemberChoco().getName(), getMemberChoco().getEmail(), "1234",
                        getMemberChoco().getRole()));
        String date = "2100-04-18";
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        reservationRepository.save(new Reservation(LocalDate.parse(date), time, theme));
        ReservationRequest reservationRequest = new ReservationRequest(date, time.getId(), theme.getId());

        //when
        ReservationResponse reservationResponse = reservationService.createMemberReservation(member,
                reservationRequest);

        //then
        assertAll(() -> assertThat(reservationResponse.date()).isEqualTo(date),
                () -> assertThat(reservationResponse.time().id()).isEqualTo(time.getId()));
    }

    @DisplayName("예약 조회에 성공한다.")
    @Test
    void find() {
        //given
        ReservationTime time = getNoon();
        Theme theme1 = themeRepository.save(getTheme1());
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        Member memberChoco = memberRepository.save(
                new MemberSignUp(getMemberChoco().getName(), getMemberChoco().getEmail(), "1234",
                        getMemberChoco().getRole()));
        memberReservationRepository.save(memberChoco, reservation1);

        Member memberClover = memberRepository.save(
                new MemberSignUp(getMemberClover().getName(), getMemberClover().getEmail(), "1234",
                        getMemberClover().getRole()));
        memberReservationRepository.save(memberClover, reservation2);

        //when
        List<ReservationResponse> reservations = reservationService.findMemberReservations(
                new ReservationQueryRequest(theme1.getId(), memberChoco.getId(), LocalDate.now(),
                        LocalDate.now().plusDays(1)));

        //then
        assertAll(() -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.get(0).date()).isEqualTo(reservation1.getDate()),
                () -> assertThat(reservations.get(0).time().id()).isEqualTo(time.getId()),
                () -> assertThat(reservations.get(0).time().startAt()).isEqualTo(time.getStartAt()));
    }

    @DisplayName("사용자 필터링 예약 조회에 성공한다.")
    @Test
    void findByMemberId() {
        //given
        ReservationTime time = getNoon();
        Theme theme = themeRepository.save(getTheme1());
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme));

        Member memberChoco = memberRepository.save(
                new MemberSignUp(getMemberChoco().getName(), getMemberChoco().getEmail(), "1234",
                        getMemberChoco().getRole()));
        memberReservationRepository.save(memberChoco, reservation);

        Member memberClover = memberRepository.save(
                new MemberSignUp(getMemberClover().getName(), getMemberClover().getEmail(), "1234",
                        getMemberClover().getRole()));
        memberReservationRepository.save(memberClover, reservation);

        //when
        List<ReservationResponse> reservations = reservationService.findMemberReservations(
                new ReservationQueryRequest(null, memberChoco.getId(), LocalDate.now(), LocalDate.now().plusDays(1)));

        //then
        assertAll(() -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.get(0).date()).isEqualTo(reservation.getDate()),
                () -> assertThat(reservations.get(0).time().id()).isEqualTo(time.getId()),
                () -> assertThat(reservations.get(0).time().startAt()).isEqualTo(time.getStartAt()));
    }

    @DisplayName("테마 필터링 예약 조회에 성공한다.")
    @Test
    void findByThemeId() {
        //given
        ReservationTime time = getNoon();
        Theme theme1 = themeRepository.save(getTheme1());
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        Member memberChoco = memberRepository.save(
                new MemberSignUp(getMemberChoco().getName(), getMemberChoco().getEmail(), "1234",
                        getMemberChoco().getRole()));
        memberReservationRepository.save(memberChoco, reservation1);
        memberReservationRepository.save(memberChoco, reservation2);

        //when
        List<ReservationResponse> reservations = reservationService.findMemberReservations(
                new ReservationQueryRequest(theme1.getId(), null, LocalDate.now(), LocalDate.now().plusDays(1)));

        //then
        assertAll(() -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.get(0).date()).isEqualTo(reservation1.getDate()),
                () -> assertThat(reservations.get(0).time().id()).isEqualTo(time.getId()),
                () -> assertThat(reservations.get(0).time().startAt()).isEqualTo(time.getStartAt()));
    }

    @DisplayName("날짜로만 예약 조회에 성공한다.")
    @Test
    void findByDate() {
        //given
        ReservationTime time = getNoon();
        Theme theme1 = themeRepository.save(getTheme1());
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        Member memberChoco = memberRepository.save(
                new MemberSignUp(getMemberChoco().getName(), getMemberChoco().getEmail(), "1234",
                        getMemberChoco().getRole()));
        memberReservationRepository.save(memberChoco, reservation1);
        memberReservationRepository.save(memberChoco, reservation2);

        //when
        List<ReservationResponse> reservations = reservationService.findMemberReservations(
                new ReservationQueryRequest(theme1.getId(), null, LocalDate.now(), LocalDate.now().plusDays(1)));

        //then
        assertAll(() -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.get(0).date()).isEqualTo(reservation1.getDate()),
                () -> assertThat(reservations.get(0).time().id()).isEqualTo(time.getId()),
                () -> assertThat(reservations.get(0).time().startAt()).isEqualTo(time.getStartAt()));
    }

    @DisplayName("예약 삭제에 성공한다.")
    @Test
    void delete() {
        //given
        ReservationTime time = getNoon();
        Theme theme = getTheme1();
        Reservation reservation = getNextDayReservation(time, theme);
        reservationRepository.save(reservation);
        Member member = memberRepository.save(
                new MemberSignUp(getMemberChoco().getName(), getMemberChoco().getEmail(), "1234",
                        getMemberChoco().getRole()));
        long id = memberReservationRepository.save(member, reservation);

        //when
        reservationService.deleteMemberReservation(member, id);

        //then
        assertThat(memberReservationRepository.findBy(null, null, LocalDate.now(), LocalDate.now().plusDays(1))).hasSize(0);
    }

    @DisplayName("일자와 시간 중복 시 예외가 발생한다.")
    @Test
    void duplicatedReservation() {
        //given
        Member member = memberRepository.save(
                new MemberSignUp(getMemberChoco().getName(), getMemberChoco().getEmail(), "1234",
                        getMemberChoco().getRole()));
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme));
        memberReservationRepository.save(member, reservation);

        ReservationRequest reservationRequest = new ReservationRequest(reservation.getDate().toString(), time.getId(),
                theme.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.createMemberReservation(member, reservationRequest)).isInstanceOf(
                BusinessException.class).hasMessage(ErrorType.DUPLICATED_RESERVATION_ERROR.getMessage());
    }

    @DisplayName("예약 삭제 시, 사용자 예약도 함께 삭제된다.")
    @Test
    void deleteMemberReservation() {
        //given
        Member member = memberRepository.save(
                new MemberSignUp(getMemberChoco().getName(), getMemberChoco().getEmail(), "1234",
                        getMemberChoco().getRole()));
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme));
        memberReservationRepository.save(member, reservation);

        //when
        reservationService.delete(reservation.getId());

        //then
        assertThat(reservationService.findMemberReservations(
                new ReservationQueryRequest(theme.getId(), member.getId(), LocalDate.now(),
                        LocalDate.now().plusDays(1)))).hasSize(0);
    }
}
