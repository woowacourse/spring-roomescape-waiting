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
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.controller.dto.MyReservationResponse;
import roomescape.reservation.controller.dto.ReservationQueryRequest;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.util.ServiceTest;

@DisplayName("예약 로직 테스트")
class ReservationServiceTest extends ServiceTest {
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationTimeRepository reservationTimeRepository;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberReservationRepository memberReservationRepository;
    @Autowired
    ReservationService reservationService;
    ReservationTime time;
    Theme theme1;
    Member memberChoco;

    @BeforeEach
    void setUp() {
        time = reservationTimeRepository.save(getNoon());
        theme1 = themeRepository.save(getTheme1());
        memberChoco = memberRepository.save(getMemberChoco());
    }

    @DisplayName("예약 생성에 성공한다.")
    @Test
    void create() {
        //given
        String date = "2100-04-18";
        reservationRepository.save(new Reservation(LocalDate.parse(date), time, theme1));
        ReservationRequest reservationRequest = new ReservationRequest(date, time.getId(), theme1.getId());

        //when
        ReservationResponse reservationResponse = reservationService.createMemberReservation(AuthInfo.of(memberChoco),
                reservationRequest);

        //then
        assertAll(() -> assertThat(reservationResponse.date()).isEqualTo(date),
                () -> assertThat(reservationResponse.time().id()).isEqualTo(time.getId()));
    }

    @DisplayName("예약 조회에 성공한다.")
    @Test
    void find() {
        //given
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        memberReservationRepository.save(new MemberReservation(memberChoco, reservation1));

        Member memberClover = memberRepository.save(getMemberClover());
        memberReservationRepository.save(new MemberReservation(memberClover, reservation2));

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
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme1));

        memberReservationRepository.save(new MemberReservation(memberChoco, reservation));

        Member memberClover = memberRepository.save(getMemberClover());
        memberReservationRepository.save(new MemberReservation(memberClover, reservation));

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
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        memberReservationRepository.save(new MemberReservation(memberChoco, reservation1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation2));

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
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        memberReservationRepository.save(new MemberReservation(memberChoco, reservation1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation2));

        //when
        List<ReservationResponse> reservations = reservationService.findMemberReservations(
                new ReservationQueryRequest(theme1.getId(), null, LocalDate.now(), LocalDate.now().plusDays(2)));

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
        Reservation reservation = getNextDayReservation(time, theme1);
        reservationRepository.save(reservation);
        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(memberChoco, reservation));

        //when
        reservationService.deleteMemberReservation(AuthInfo.of(memberChoco), memberReservation.getId());

        //then
        assertThat(
                memberReservationRepository.findBy(null, null, LocalDate.now(), LocalDate.now().plusDays(1))).hasSize(
                0);
    }

    @DisplayName("일자와 시간 중복 시 예외가 발생한다.")
    @Test
    void duplicatedReservation() {
        //given
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation));

        ReservationRequest reservationRequest = new ReservationRequest(reservation.getDate().toString(), time.getId(),
                theme1.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.createMemberReservation(AuthInfo.of(memberChoco),
                reservationRequest)).isInstanceOf(
                BusinessException.class).hasMessage(ErrorType.DUPLICATED_RESERVATION_ERROR.getMessage());
    }

    @DisplayName("예약 삭제 시, 사용자 예약도 함께 삭제된다.")
    @Test
    void deleteMemberReservation() {
        //given
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation));

        //when
        reservationService.delete(reservation.getId());

        //then
        assertThat(reservationService.findMemberReservations(
                new ReservationQueryRequest(theme1.getId(), memberChoco.getId(), LocalDate.now(),
                        LocalDate.now().plusDays(1)))).hasSize(0);
    }

    @DisplayName("나의 예약 조회에 성공한다.")
    @Test
    void myReservations() {
        //given
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        memberReservationRepository.save(new MemberReservation(memberChoco, reservation1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation2));

        //when
        List<MyReservationResponse> myReservations = reservationService.findMyReservations(AuthInfo.of(memberChoco));

        //then
        assertAll(
                () -> assertThat(myReservations).hasSize(2),
                () -> assertThat(myReservations).extracting(MyReservationResponse::time).containsOnly(time.getStartAt())
        );
    }
}
