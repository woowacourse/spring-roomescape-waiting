package roomescape.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.reservation.ReservationStatus.CONFIRMED;
import static roomescape.domain.reservation.ReservationStatus.REJECTED;
import static roomescape.domain.reservation.ReservationStatus.WAITING;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.reservation.DateTimePassedException;
import roomescape.exception.reservation.ReservationDuplicatedException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.exception.reservation.ThemeNotFoundException;
import roomescape.exception.reservation.TimeNotFoundException;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.ReservationRankResponse;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationServiceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    private final Member member = new Member("tt@tt.com", "123", "영이", "MEMBER");
    private final ReservationTime time1 = new ReservationTime("11:00");
    private final Theme theme = new Theme("공포", "공포는 무서워", "hi.jpg");
    private final LocalDate date1 = LocalDate.parse("2025-11-30");

    @DisplayName("회원의 예약 정보를 조회하는데 회원 이메일이 DB에 존재하지 않으면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_not_exists_member_email() {
        memberRepository.save(member);
        themeRepository.save(theme);
        reservationTimeRepository.save(time1);
        Reservation reservation = new Reservation(member, theme, date1, time1, CONFIRMED);
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> reservationService.findAllReservationsByEmail("t1@t1.com"))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @DisplayName("회원의 예약 정보를 조회하면 목록이 (날짜, 시간) 순서대로 정렬되어야 한다.")
    @Test
    void reservations_list_should_be_sorted_by_date_and_time() {
        memberRepository.save(member);
        themeRepository.save(theme);
        ReservationTime time2 = new ReservationTime("12:00");
        LocalDate date2 = LocalDate.parse("2025-12-01");
        reservationTimeRepository.save(time1);
        reservationTimeRepository.save(time2);
        Reservation reservation1 = new Reservation(member, theme, date2, time1, CONFIRMED);
        Reservation reservation2 = new Reservation(member, theme, date2, time2, CONFIRMED);
        Reservation reservation3 = new Reservation(member, theme, date1, time2, CONFIRMED);
        Reservation reservation4 = new Reservation(member, theme, date1, time1, CONFIRMED);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        reservationRepository.save(reservation4);
        List<ReservationRankResponse> expected = List.of(
                new ReservationRankResponse(4L, theme.getName(), date1, time1.getStartAt(), CONFIRMED, 1),
                new ReservationRankResponse(3L, theme.getName(), date1, time2.getStartAt(), CONFIRMED, 1),
                new ReservationRankResponse(1L, theme.getName(), date2, time1.getStartAt(), CONFIRMED, 1),
                new ReservationRankResponse(2L, theme.getName(), date2, time2.getStartAt(), CONFIRMED, 1)
        );

        List<ReservationRankResponse> actual = reservationService.findAllReservationsByEmail(member.getEmail());

        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @DisplayName("저장되어있지 않은 예약 시간에 예약을 시도하면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_unsaved_time() {
        memberRepository.save(member);
        themeRepository.save(theme);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2025-11-30", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("저장되어있지 않은 테마에 예약을 시도하면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_unsaved_theme() {
        memberRepository.save(member);
        reservationTimeRepository.save(time1);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2025-11-30", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("이미 지난 날짜에 예약을 시도하면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_before_date() {
        memberRepository.save(member);
        reservationTimeRepository.save(time1);
        themeRepository.save(theme);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2024-05-07", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(DateTimePassedException.class);
    }

    @DisplayName("같은 테마, 날짜, 시간에 예약, 예약 대기를 중복해서 시도하면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_create_duplicated_reservation_or_reservation_waiting() {
        memberRepository.save(member);
        reservationTimeRepository.save(time1);
        themeRepository.save(theme);
        Reservation reservation = new Reservation(member, theme, date1, time1, CONFIRMED);
        reservationRepository.save(reservation);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2025-11-30", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(ReservationDuplicatedException.class);
    }

    @DisplayName("같은 테마, 날짜, 시간에 대기중인 예약이 존재할 경우 예약의 상태를 대기로 생성한다.")
    @Test
    void return_reservation_status_waiting_when_exists_waiting_same_theme_and_date_time() {
        Member another = new Member("t1@t1.com", "123", "재즈", "MEMBER");
        memberRepository.save(another);
        memberRepository.save(member);
        reservationTimeRepository.save(time1);
        themeRepository.save(theme);
        Reservation reservation = new Reservation(member, theme, date1, time1, CONFIRMED);
        reservationRepository.save(reservation);

        ReservationCreate reservationDto = new ReservationCreate("t1@t1.com", 1L, "2025-11-30", 1L);
        ReservationResponse actual = reservationService.createReservation(reservationDto);

        assertThat(actual.getStatus()).isEqualTo(WAITING.toString());
    }

    @DisplayName("같은 테마, 날짜, 시간에 대기중인 예약이 존재하지 않으면 예약의 상태를 예약으로 생성한다.")
    @Test
    void throw_exception_when_create_reservation_use_same_theme_and_date_time() {
        memberRepository.save(member);
        reservationTimeRepository.save(time1);
        themeRepository.save(theme);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2025-11-30", 1L);
        ReservationResponse actual = reservationService.createReservation(reservationDto);

        assertThat(actual.getStatus()).isEqualTo(CONFIRMED.toString());
    }

    @DisplayName("예약을 정상적으로 생성한다.")
    @Test
    void success_create_reservation() {
        reservationTimeRepository.save(time1);
        themeRepository.save(theme);
        memberRepository.save(member);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2025-11-30", 1L);

        assertThatNoException()
                .isThrownBy(() -> reservationService.createReservation(reservationDto));
    }

    @DisplayName("대기 상태인 예약 취소 시 회원 이메일이 DB에 존재하지 않으면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_delete_reservation_waiting_not_saved_reservation_id() {
        assertThatThrownBy(() -> reservationService.cancelWaitingReservation("t1@t1.com", 1L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @DisplayName("취소 하려는 대기 예약이 대기 상태가 아니거나 DB에 존재하지 않을 경우 예외를 발생시킨다.")
    @Test
    void throw_exception_when_not_waiting_status_or_not_saved_reservation_id() {
        memberRepository.save(member);
        reservationTimeRepository.save(time1);
        themeRepository.save(theme);
        Reservation reservation = new Reservation(member, theme, date1, time1, CONFIRMED);
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> reservationService.cancelWaitingReservation("tt@tt.com", 1L))
                .isInstanceOf(ReservationNotFoundException.class);
    }


    @DisplayName("대기 상태인 예약을 정상적으로 취소 상태로 변경한다.")
    @Test
    void throw_exception_when_not_saved_reservation_id() {
        memberRepository.save(member);
        reservationTimeRepository.save(time1);
        themeRepository.save(theme);
        Reservation reservation = new Reservation(member, theme, date1, time1, WAITING);
        reservationRepository.save(reservation);

        assertThatNoException()
                .isThrownBy(() -> reservationService.cancelWaitingReservation("tt@tt.com", 1L));
    }

    @DisplayName("거절하려는 예약이 DB에 존재하지 않으면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_delete_not_exists_reservation() {
        memberRepository.save(member);
        reservationTimeRepository.save(time1);
        themeRepository.save(theme);

        assertThatThrownBy(() -> reservationService.rejectConfirmedReservation(1L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @DisplayName("예약을 정상적으로 거절 상태로 변경하고 만약 대기중인 예약이 존재하면 가장 우선 대기중인 예약을 확정 상태로 변경한다.")
    @Test
    void success_delete_reservation_with_change_status_waiting_reservation_to_confirmed() {
        Member another = new Member("t1@t1.com", "123", "재즈", "MEMBER");
        memberRepository.save(another);
        memberRepository.save(member);
        reservationTimeRepository.save(time1);
        themeRepository.save(theme);
        Reservation reservation1 = new Reservation(member, theme, date1, time1, CONFIRMED);
        Reservation reservation2 = new Reservation(another, theme, date1, time1, WAITING);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);

        reservationService.rejectConfirmedReservation(1L);
        Reservation reservation = reservationRepository.findById(2L).get();

        assertThat(reservation.getReservationStatus()).isEqualTo(CONFIRMED);
    }

    @DisplayName("거절하려는 예약 대기가 DB에 존재하지 않으면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_delete_not_exists_waiting_reservation() {
        memberRepository.save(member);
        reservationTimeRepository.save(time1);
        themeRepository.save(theme);

        assertThatThrownBy(() -> reservationService.rejectWaitingReservation(1L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @DisplayName("예약 대기를 정상적으로 거절 상태로 변경한다.")
    @Test
    void success_reject_waiting_reservation() {
        memberRepository.save(member);
        reservationTimeRepository.save(time1);
        themeRepository.save(theme);
        Reservation reservation = new Reservation(member, theme, date1, time1, WAITING);
        reservationRepository.save(reservation);

        reservationService.rejectWaitingReservation(1L);

        Reservation savedReservation = reservationRepository.findById(1L).get();
        assertThat(savedReservation.getReservationStatus()).isEqualTo(REJECTED);
    }
}
