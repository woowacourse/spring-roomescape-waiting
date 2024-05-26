package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.controller.dto.ReservationQueryRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.service.dto.MyReservationInfo;
import roomescape.reservation.service.services.MemberReservationService;
import roomescape.util.ServiceTest;

@DisplayName("사용자 예약 로직 테스트")
class MemberReservationServiceTest extends ServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberReservationService memberReservationService;

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
        LocalDate date = LocalDate.now().plusMonths(1);
        Reservation reservation = reservationRepository.save(new Reservation(date, time, theme1));

        //when
        MemberReservation memberReservation = memberReservationService.createMemberReservation(memberChoco,
                reservation);

        //then
        assertAll(
                () -> assertThat(memberReservation.getReservationStatus()).isEqualTo(ReservationStatus.APPROVED),
                () -> assertThat(memberReservation.getMember()).isEqualTo(memberChoco),
                () -> assertThat(memberReservation.getReservation().getDate()).isEqualTo(date),
                () -> assertThat(memberReservation.getReservation().getTime()).isEqualTo(time),
                () -> assertThat(memberReservation.getReservation().getTheme()).isEqualTo(theme1)
        );
    }

    @DisplayName("예약 조회에 성공한다.")
    @Test
    void find() {
        //given
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        memberReservationRepository.save(new MemberReservation(memberChoco, reservation1, ReservationStatus.APPROVED));

        Member memberClover = memberRepository.save(getMemberClover());
        memberReservationRepository.save(new MemberReservation(memberClover, reservation2, ReservationStatus.APPROVED));

        //when
        List<ReservationResponse> reservations = memberReservationService.findMemberReservations(
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

        memberReservationRepository.save(new MemberReservation(memberChoco, reservation, ReservationStatus.APPROVED));

        Member memberClover = memberRepository.save(getMemberClover());
        memberReservationRepository.save(new MemberReservation(memberClover, reservation, ReservationStatus.APPROVED));

        //when
        List<ReservationResponse> reservations = memberReservationService.findMemberReservations(
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

        memberReservationRepository.save(new MemberReservation(memberChoco, reservation1, ReservationStatus.APPROVED));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation2, ReservationStatus.APPROVED));

        //when
        List<ReservationResponse> reservations = memberReservationService.findMemberReservations(
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

        memberReservationRepository.save(new MemberReservation(memberChoco, reservation1, ReservationStatus.APPROVED));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation2, ReservationStatus.APPROVED));

        //when
        List<ReservationResponse> reservations = memberReservationService.findMemberReservations(
                new ReservationQueryRequest(theme1.getId(), null, LocalDate.now(), LocalDate.now().plusDays(2)));

        //then
        assertAll(() -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.get(0).date()).isEqualTo(reservation1.getDate()),
                () -> assertThat(reservations.get(0).time().id()).isEqualTo(time.getId()),
                () -> assertThat(reservations.get(0).time().startAt()).isEqualTo(time.getStartAt()));
    }

    @DisplayName("예약 상태 변경에 성공한다.")
    @Test
    void updateStatus() {
        //given
        Reservation reservation = getNextDayReservation(time, theme1);
        reservationRepository.save(reservation);
        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(memberChoco, reservation, ReservationStatus.APPROVED));

        //when
        memberReservationService.updateStatus(memberReservation, ReservationStatus.PENDING, ReservationStatus.APPROVED);

        //then
        assertThat(
                memberReservationRepository.findBy(null, null, ReservationStatus.APPROVED, LocalDate.now(),
                        LocalDate.now().plusDays(1))).hasSize(1);
    }

    @DisplayName("예약 삭제 시, 사용자 예약도 함께 삭제된다.")
    @Test
    void deleteMemberReservation() {
        //given
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation, ReservationStatus.APPROVED));

        //when
        memberReservationService.delete(reservation.getId());

        //then
        assertThat(memberReservationService.findMemberReservations(
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

        memberReservationRepository.save(new MemberReservation(memberChoco, reservation1, ReservationStatus.APPROVED));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation2, ReservationStatus.APPROVED));

        //when
        List<MyReservationInfo> myReservations = memberReservationService.findMyReservations(memberChoco);

        //then
        assertThat(myReservations).hasSize(2);
    }
}
