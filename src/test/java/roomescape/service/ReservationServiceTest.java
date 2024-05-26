package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Schedule;
import roomescape.entity.Theme;
import roomescape.entity.Waiting;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.response.MemberResponse;
import roomescape.service.dto.response.MyReservationEntryResponse;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;

    @BeforeEach
    void insertReservation() {
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        theme = themeRepository.save(new Theme("happy", "hi", "abcd.html"));
        member = memberRepository.save(new Member("sudal", "sudal@email.com", "sudal", Role.USER));
    }

    @DisplayName("예약 생성 테스트")
    @Test
    void createReservation() {
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2030, 12, 12),
                reservationTime.getId(), theme.getId(), member.getId());

        ReservationResponse reservationResponse = reservationService.createReservation(reservationRequest,
                reservationRequest.memberId());

        assertAll(
                () -> assertThat(reservationResponse.name()).isEqualTo(member.getName()),
                () -> assertThat(reservationResponse.date()).isEqualTo(LocalDate.of(2030, 12, 12)),
                () -> assertThat(reservationResponse.theme()).isEqualTo(ThemeResponse.from(theme)),
                () -> assertThat(reservationResponse.member()).isEqualTo(MemberResponse.from(member)),
                () -> assertThat(reservationResponse.time()).isEqualTo(ReservationTimeResponse.from(reservationTime))
        );
    }

    @DisplayName("예약시간이 없는 경우 예외가 발생한다.")
    @Test
    void reservationTimeIsNotExist() {
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2030, 12, 12), 1000L, theme.getId(),
                1L);

        assertThatThrownBy(
                () -> reservationService.createReservation(reservationRequest, reservationRequest.memberId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.NOT_FOUND_RESERVATION_TIME.getErrorMessage());
    }

    @DisplayName("과거 시간을 예약하는 경우 예외가 발생한다.")
    @Test
    void validatePastTime() {
        ReservationTime reservationTime = reservationTimeRepository.save(
                new ReservationTime(LocalTime.of(10, 0)));

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(1999, 12, 12),
                reservationTime.getId(),
                theme.getId(), 1L);

        assertThatThrownBy(
                () -> reservationService.createReservation(reservationRequest, reservationRequest.memberId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.PAST_TIME_SLOT_RESERVATION.getErrorMessage());
    }

    @DisplayName("모든 예약 조회 테스트")
    @Test
    void findAllReservations() {
        List<Reservation> reservations = List.of(
                new Reservation(member, new Schedule(LocalDate.of(2999, 12, 12), reservationTime, theme)),
                new Reservation(member, new Schedule(LocalDate.of(2999, 12, 13), reservationTime, theme)));

        reservationRepository.saveAll(reservations);

        List<ReservationResponse> findReservations = reservationService.findAllReservations();

        assertThat(findReservations).hasSize(2);
    }

    @DisplayName("예약 삭제 테스트")
    @Test
    void deleteReservation() {
        Reservation reservation = reservationRepository.save(
                new Reservation(member, new Schedule(LocalDate.of(2999, 12, 12), reservationTime, theme)));

        reservationService.deleteReservation(reservation.getId());

        List<Reservation> findReservations = reservationRepository.findAll();
        assertThat(findReservations).isEmpty();
    }

    @DisplayName("예약이 삭제되면 첫번째 예약 대기는 예약으로 전환된다.")
    @Test
    void convertFirstWaitingToReservation() {
        List<Reservation> reservations = List.of(
                new Reservation(member, new Schedule(LocalDate.of(2999, 12, 12), reservationTime, theme)),
                new Reservation(member, new Schedule(LocalDate.of(2999, 12, 13), reservationTime, theme)));

        List<Waiting> waitings = List.of(
                new Waiting(member, new Schedule(LocalDate.of(2999, 12, 11), reservationTime, theme)),
                new Waiting(member, new Schedule(LocalDate.of(2999, 12, 12), reservationTime, theme)),
                new Waiting(member, new Schedule(LocalDate.of(2999, 12, 13), reservationTime, theme)));

        reservationRepository.saveAll(reservations);
        waitingRepository.saveAll(waitings);

        reservationService.deleteReservation(1L);

        List<Reservation> allReservations = reservationRepository.findAll();
        List<Waiting> allWaitings = waitingRepository.findAll();

        assertAll(
                () -> assertThat(allReservations.get(1).getSchedule().getDate()).isEqualTo(LocalDate.of(2999, 12, 12)),
                () -> assertThat(allWaitings.get(1).getSchedule().getDate()).isNotEqualTo(LocalDate.of(2999, 12, 12))
        );
    }

    @DisplayName("사용자별 모든 예약 조회 테스트")
    @Test
    void findAllByMemberId() {
        ReservationTime reservationTime1 = reservationTimeRepository.save(
                new ReservationTime(LocalTime.of(10, 0)));
        ReservationTime reservationTime2 = reservationTimeRepository.save(
                new ReservationTime(LocalTime.of(12, 0)));

        Member member1 = memberRepository.save(
                new Member("sudal", "sudal@email.com", "sudal123", Role.ADMIN));
        Member member2 = memberRepository.save(
                new Member("rush", "rush@email.com", "rush", Role.ADMIN));

        reservationRepository.save(new Reservation(member1,new Schedule( LocalDate.of(2030, 12, 12), reservationTime1, theme)));
        reservationRepository.save(new Reservation(member2,new Schedule( LocalDate.of(2030, 12, 12), reservationTime2, theme)));

        List<MyReservationEntryResponse> reservations = reservationService.findAllByMemberId(member1.getId());

        assertThat(reservations).hasSize(1);
    }
}
