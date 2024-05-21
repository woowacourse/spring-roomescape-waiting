package roomescape.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.reservation.ReservationStatus.CONFIRMED;
import static roomescape.domain.reservation.ReservationStatus.WAITING;

import io.restassured.RestAssured;
import java.time.LocalDate;
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
import roomescape.exception.reservation.TimeNotFoundException;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.exception.ThemeNotFoundException;
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
    private final ReservationTime time = new ReservationTime("11:00");
    private final Theme theme = new Theme("공포", "공포는 무서워", "hi.jpg");
    private final LocalDate date = LocalDate.parse("2025-11-30");

    @DisplayName("회원의 예약 정보를 조회하는데 회원 이메일이 DB에 존재하지 않으면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_not_exists_member_email() {
        memberRepository.save(member);
        themeRepository.save(theme);
        reservationTimeRepository.save(time);
        Reservation reservation = new Reservation(member, theme, date, time, CONFIRMED);
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> reservationService.findReservationsByMemberEmail("t1@t1.com"))
                .isInstanceOf(MemberNotFoundException.class);
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
        reservationTimeRepository.save(time);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2025-11-30", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("이미 지난 날짜에 예약을 시도하면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_before_date() {
        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2024-05-07", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(DateTimePassedException.class);
    }

    @DisplayName("같은 테마, 날짜, 시간에 예약, 예약 대기를 중복해서 시도하면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_create_duplicated_reservation_or_reservation_waiting() {
        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        Reservation reservation = new Reservation(member, theme, date, time, CONFIRMED);
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
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        Reservation reservation = new Reservation(member, theme, date, time, CONFIRMED);
        reservationRepository.save(reservation);

        ReservationCreate reservationDto = new ReservationCreate("t1@t1.com", 1L, "2025-11-30", 1L);
        ReservationResponse actual = reservationService.createReservation(reservationDto);

        assertThat(actual.getReservationStatus()).isEqualTo(WAITING.toString());
    }

    @DisplayName("같은 테마, 날짜, 시간에 대기중인 예약이 존재하지 않으면 예약의 상태를 예약으로 생성한다.")
    @Test
    void throw_exception_when_create_reservation_use_same_theme_and_date_time() {
        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2025-11-30", 1L);
        ReservationResponse actual = reservationService.createReservation(reservationDto);

        assertThat(actual.getReservationStatus()).isEqualTo(CONFIRMED.toString());
    }

    @DisplayName("예약을 정상적으로 생성한다.")
    @Test
    void success_create_reservation() {
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        memberRepository.save(member);

        ReservationCreate reservationDto = new ReservationCreate("tt@tt.com", 1L, "2025-11-30", 1L);

        assertThatNoException()
                .isThrownBy(() -> reservationService.createReservation(reservationDto));
    }

    @DisplayName("대기 상태인 예약 삭제 시 회원 이메일이 DB에 존재하지 않으면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_delete_reservation_waiting_not_saved_reservation_id() {
        assertThatThrownBy(() -> reservationService.cancelWaitingReservation("t1@t1.com", 1L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @DisplayName("삭제 하려는 대기 예약이 대기 상태가 아니거나 DB에 존재하지 않을 경우 예외를 발생시킨다.")
    @Test
    void throw_exception_when_not_waiting_status_or_not_saved_reservation_id() {
        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        Reservation reservation = new Reservation(member, theme, date, time, CONFIRMED);
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> reservationService.cancelWaitingReservation("tt@tt.com", 1L))
                .isInstanceOf(ReservationNotFoundException.class);
    }


    @DisplayName("대기 상태인 예약을 정상적으로 삭제한다.")
    @Test
    void throw_exception_when_not_saved_reservation_id() {
        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        Reservation reservation = new Reservation(member, theme, date, time, WAITING);
        reservationRepository.save(reservation);

        assertThatNoException()
                .isThrownBy(() -> reservationService.cancelWaitingReservation("tt@tt.com", 1L));
    }

    @DisplayName("삭제하려는 예약이 DB에 존재하지 않으면 예외를 발생시킨다.")
    @Test
    void throw_exception_when_delete_not_exists_reservation() {
        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);

        assertThatThrownBy(() -> reservationService.cancelConfirmedReservation(1L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @DisplayName("예약을 정상적으로 삭제하고 만약 대기중인 예약이 존재하면 가장 우선 대기중인 예약을 확정 상태로 변경한다.")
    @Test
    void success_delete_reservation_with_change_status_waiting_reservation_to_confirmed() {
        Member another = new Member("t1@t1.com", "123", "재즈", "MEMBER");
        memberRepository.save(another);
        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        Reservation reservation1 = new Reservation(member, theme, date, time, CONFIRMED);
        Reservation reservation2 = new Reservation(another, theme, date, time, WAITING);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);

        reservationService.cancelConfirmedReservation(1L);
        Reservation reservation = reservationRepository.findById(2L).get();

        assertThat(reservation.getReservationStatus()).isEqualTo(CONFIRMED);
    }
}
