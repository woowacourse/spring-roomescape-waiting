package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.*;
import static roomescape.fixture.ReservationFixture.getNextDayReservation;
import static roomescape.fixture.ReservationTimeFixture.getNoon;
import static roomescape.fixture.ThemeFixture.getTheme1;
import static roomescape.fixture.ThemeFixture.getTheme2;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.custom.ForbiddenException;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.controller.dto.*;
import roomescape.reservation.domain.*;
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
    MemberReservationService memberReservationService;
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

    @DisplayName("예약 생성에 성공한다.")
    @Test
    void create() {
        //given
        Member member = memberRepository.save(getMemberChoco());
        String date = "2100-04-18";
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        reservationRepository.save(new Reservation(LocalDate.parse(date), time, theme));
        ReservationRequest reservationRequest = new ReservationRequest(date, time.getId(), theme.getId());

        //when
        ReservationResponse reservationResponse = memberReservationService.createMemberReservation(AuthInfo.of(member),
                reservationRequest);

        //then
        assertAll(() -> assertThat(reservationResponse.date()).isEqualTo(date),
                () -> assertThat(reservationResponse.time().id()).isEqualTo(time.getId()));
    }

    @DisplayName("예약 조회에 성공한다.")
    @Test
    void find() {
        //given
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme1 = themeRepository.save(getTheme1());
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        Member memberChoco = memberRepository.save(getMemberChoco());
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation1));

        Member memberClover = memberRepository.save(getMemberClover());
        memberReservationRepository.save(new MemberReservation(memberClover, reservation2));

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
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme));

        Member memberChoco = memberRepository.save(getMemberChoco());
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation));

        Member memberClover = memberRepository.save(getMemberClover());
        memberReservationRepository.save(new MemberReservation(memberClover, reservation));

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
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme1 = themeRepository.save(getTheme1());
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        Member memberChoco = memberRepository.save(getMemberChoco());
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation2));

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
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme1 = themeRepository.save(getTheme1());
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        Member memberChoco = memberRepository.save(getMemberChoco());
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation2));

        //when
        List<ReservationResponse> reservations = memberReservationService.findMemberReservations(
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
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        Reservation reservation = getNextDayReservation(time, theme);
        reservationRepository.save(reservation);
        Member member = memberRepository.save(getMemberChoco());
        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(member, reservation));

        //when
        memberReservationService.deleteMemberReservation(AuthInfo.of(member), memberReservation.getId());

        //then
        assertThat(
                memberReservationRepository.findBy(null, null, LocalDate.now(), LocalDate.now().plusDays(1))).hasSize(
                0);
    }

    @DisplayName("일자와 시간 중복 시 예외가 발생한다.")
    @Test
    void duplicatedReservation() {
        //given
        Member member = memberRepository.save(getMemberChoco());
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme));
        memberReservationRepository.save(new MemberReservation(member, reservation));

        ReservationRequest reservationRequest = new ReservationRequest(reservation.getDate().toString(), time.getId(),
                theme.getId());

        //when & then
        assertThatThrownBy(() -> memberReservationService.createMemberReservation(AuthInfo.of(member), reservationRequest))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("예약 삭제 시, 사용자 예약도 함께 삭제된다.")
    @Test
    void deleteMemberReservation() {
        //given
        Member member = memberRepository.save(getMemberChoco());
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme));
        memberReservationRepository.save(new MemberReservation(member, reservation));

        //when
        reservationService.delete(reservation.getId());

        //then
        assertThat(memberReservationService.findMemberReservations(
                new ReservationQueryRequest(theme.getId(), member.getId(), LocalDate.now(),
                        LocalDate.now().plusDays(1)))).hasSize(0);
    }

    @DisplayName("나의 예약 조회에 성공한다.")
    @Test
    void myReservations() {
        //given
        Member member = memberRepository.save(getMemberClover());
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme1 = themeRepository.save(getTheme1());
        Theme theme2 = themeRepository.save(getTheme2());
        Reservation reservation1 = reservationRepository.save(getNextDayReservation(time, theme1));
        Reservation reservation2 = reservationRepository.save(getNextDayReservation(time, theme2));

        memberReservationRepository.save(new MemberReservation(member, reservation1));
        memberReservationRepository.save(new MemberReservation(member, reservation2));

        //when
        List<MyReservationWithStatus> myReservations = reservationService.findMyReservations(AuthInfo.of(member));

        //then
        assertAll(
                () -> assertThat(myReservations).hasSize(2),
                () -> assertThat(myReservations).extracting(MyReservationWithStatus::time).containsOnly(time.getStartAt())
        );
    }

    @DisplayName("앞선 예약이 있는 경우 예약을 대기한다")
    @Test
    void existSameReservation() {
        //given
        Reservation reservation = getNextDayReservation(ReservationTimeFixture.get1PM(), getTheme1());
        Member choco = getMemberChoco();
        Member tacan = getMemberTacan();

        Reservation reservation1 = reservationRepository.save(reservation);
        MemberReservation save = memberReservationRepository.save(new MemberReservation(choco, reservation));

        //when
        ReservationResponse memberReservation1 = memberReservationService.createMemberReservation(new MemberReservationRequest(
                tacan.getId(),
                reservation.getDate().format(DateTimeFormatter.ISO_DATE),
                reservation.getTime().getId(),
                reservation.getTheme().getId(
                )));
        List<MemberReservation> allByMember = memberReservationRepository.findAllByMember(tacan);
        MemberReservation addedMemberReservation = allByMember
                .stream()
                .filter(memberReservation -> Objects.equals(memberReservation.getReservation().getId(), reservation.getId()))
                .findAny()
                .get();

        //then
        assertThat(addedMemberReservation.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

}
