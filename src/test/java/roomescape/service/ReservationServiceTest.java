package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;

import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationWithWaitingResult;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("조건이 없다면 전체 예약을 조회한다.")
    @Test
    void getAllReservations() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation1 = reservationRepository.save(TestFixture.createNewReservation(member, DEFAULT_DATE, reservationTime, theme));
        Reservation reservation2 = reservationRepository.save(TestFixture.createNewReservation(member, DEFAULT_DATE.plusDays(1), reservationTime, theme));

        //when
        List<ReservationResult> reservationResults = reservationService.getReservationsInConditions(null, null, null, null);

        //then
        assertAll(
                () -> assertThat(reservationResults).hasSize(2),
                () -> assertThat(reservationResults)
                        .isEqualTo(List.of(
                                ReservationResult.from(reservation1),
                                ReservationResult.from(reservation2)
                        ))
        );
    }

    @DisplayName("조건이 있다면 필터링하여 조회한다.")
    @Test
    void getReservationsInConditions() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member1 = memberRepository.save(TestFixture.createMemberByName("member1"));
        Member member2 = memberRepository.save(TestFixture.createMemberByName("member2"));
        Reservation reservation1 = reservationRepository.save(TestFixture.createNewReservation(member1, DEFAULT_DATE, reservationTime, theme));
        Reservation reservation2 = reservationRepository.save(TestFixture.createNewReservation(member2, DEFAULT_DATE, reservationTime, theme));


        //when
        List<ReservationResult> reservationResults = reservationService.getReservationsInConditions(member1.getId(), null, null, null);

        //then
        assertAll(
                () -> assertThat(reservationResults).hasSize(1),
                () -> assertThat(reservationResults)
                        .isEqualTo(List.of(ReservationResult.from(reservation1)))
        );
    }

    @DisplayName("대기 상태의 예약을 모두 조회할 수 있다.")
    @Test
    void getWaitingReservations() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation waiting1 = reservationRepository.save(TestFixture.createWaitingReservation(member, DEFAULT_DATE, reservationTime, theme));
        Reservation waiting2 = reservationRepository.save(TestFixture.createWaitingReservation(member, DEFAULT_DATE.plusDays(1), reservationTime, theme));

        //when
        List<ReservationResult> reservationResults = reservationService.getWaitingReservations();

        //then
        assertAll(
                () -> assertThat(reservationResults).hasSize(2),
                () -> assertThat(reservationResults)
                        .isEqualTo(List.of(
                                ReservationResult.from(waiting1),
                                ReservationResult.from(waiting2)
                        ))
        );
    }

    @DisplayName("회원ID로 해당 회원의 예약을 모두 조회할 수 있다.")
    @Test
    void getMemberReservationsById() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation waiting1 = reservationRepository.save(TestFixture.createWaitingReservation(member, DEFAULT_DATE, reservationTime, theme));
        Reservation waiting2 = reservationRepository.save(TestFixture.createWaitingReservation(member, DEFAULT_DATE, reservationTime, theme));

        //when
        List<ReservationWithWaitingResult> results = reservationService.getMemberReservationsById(member.getId());

        //then
        assertAll(
                () -> assertThat(results).hasSize(2),
                () -> assertThat(results)
                        .isEqualTo(List.of(
                                ReservationWithWaitingResult.from(waiting1, 1),
                                ReservationWithWaitingResult.from(waiting2, 2)
                        ))
        );
    }

    @DisplayName("예약자 ID로 예약을 삭제한다.")
    @Test
    void deleteById() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation = reservationRepository.save(TestFixture.createNewReservation(member, DEFAULT_DATE, reservationTime, theme));

        //when
        reservationService.deleteById(reservation.getId());

        //then
        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @DisplayName("예약 삭제 후 해당 시간대에 예약이 없다면 대기자를 자동으로 예약한다")
    @Test
    void deleteByIdWithAutoReservation() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member1 = memberRepository.save(TestFixture.createMemberByName("member1"));
        Member member2 = memberRepository.save(TestFixture.createMemberByName("member2"));

        Reservation reservation = reservationRepository.save(TestFixture.createNewReservation(member1, DEFAULT_DATE, reservationTime, theme));
        Reservation waiting = reservationRepository.save(TestFixture.createWaitingReservation(member2, DEFAULT_DATE, reservationTime, theme));

        //when
        reservationService.deleteById(reservation.getId());

        //then
        Reservation updatedWaiting = reservationRepository.findById(waiting.getId()).get();
        assertThat(updatedWaiting.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @DisplayName("대기를 정상적으로 취소할 수 있다")
    @Test
    void cancelWaitingByIdSuccess() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());

        Reservation waiting = reservationRepository.save(TestFixture.createWaitingReservation(member, DEFAULT_DATE, reservationTime, theme));
        LoginMemberInfo loginMemberInfo = new LoginMemberInfo(member.getId());

        //when
        reservationService.cancelWaitingById(waiting.getId(), loginMemberInfo);

        //then
        assertThat(reservationRepository.findById(waiting.getId())).isEmpty();
    }

    @DisplayName("자신의 예약이 아닌 경우 대기 취소할 수 없다")
    @Test
    void cancelWaitingByIdWithoutPermission() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member1 = memberRepository.save(TestFixture.createMemberByName("member1"));
        Member member2 = memberRepository.save(TestFixture.createMemberByName("member2"));

        Reservation waiting = reservationRepository.save(TestFixture.createWaitingReservation(member1, DEFAULT_DATE, reservationTime, theme));
        LoginMemberInfo loginMemberInfo = new LoginMemberInfo(member2.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.cancelWaitingById(waiting.getId(), loginMemberInfo))
                .isInstanceOf(DeletionNotAllowedException.class)
                .hasMessage("자신의 예약만 삭제할 수 있습니다.");
    }
}
