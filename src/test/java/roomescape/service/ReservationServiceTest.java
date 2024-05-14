package roomescape.service;


import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.restassured.RestAssured;
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
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.repository.JdbcMemberRepository;
import roomescape.repository.JdbcReservationRepository;
import roomescape.repository.JdbcReservationTimeRepository;
import roomescape.repository.JdbcThemeRepository;
import roomescape.service.dto.reservation.ReservationCreate;

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
    private JdbcReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JdbcThemeRepository themeRepository;

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @Autowired
    private JdbcMemberRepository memberRepository;

    private final Member member = new Member(1L, "t1@t1.com", "123", "러너덕", "MEMBER");
    private final ReservationTime time = new ReservationTime(1L, "11:00");
    private final Theme theme = new Theme(1L, "공포", "공포는 무서워", "hi.jpg");
    private final ReservationDate date = new ReservationDate("2025-11-30");

    @DisplayName("저장되어있지 않은 예약 시간에 예약을 시도하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_unsaved_time() {
        memberRepository.insertMember(member);
        themeRepository.insertTheme(theme);

        ReservationCreate reservationDto = new ReservationCreate(1L, 1L, "2025-11-30", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 하려는 시간이 저장되어 있지 않습니다.");
    }

    @DisplayName("이미 지나간 날짜에 예약을 시도하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_before_date() {
        memberRepository.insertMember(member);
        reservationTimeRepository.insertReservationTime(time);
        themeRepository.insertTheme(theme);

        ReservationCreate reservationDto = new ReservationCreate(1L, 1L, "2024-05-07", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지나간 날짜와 시간에 대한 예약은 불가능합니다.");
    }

    @DisplayName("같은 테마를 같은 시간에 예약을 시도하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_same_theme_and_date_time() {
        memberRepository.insertMember(member);
        reservationTimeRepository.insertReservationTime(time);
        themeRepository.insertTheme(theme);
        Reservation reservation1 = new Reservation(1L, member, theme, date, time);
        reservationRepository.insertReservation(reservation1);

        ReservationCreate reservationDto = new ReservationCreate(1L, 1L, "2025-11-30", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 테마는 같은 시간에 이미 예약이 존재합니다.");
    }

    @DisplayName("예약을 정상적으로 생성한다.")
    @Test
    void success_create_reservation() {
        reservationTimeRepository.insertReservationTime(time);
        themeRepository.insertTheme(theme);
        memberRepository.insertMember(member);

        ReservationCreate reservationDto = new ReservationCreate(1L, 1L, "2025-11-30", 1L);

        assertThatNoException()
                .isThrownBy(() -> reservationService.createReservation(reservationDto));
    }


    @DisplayName("예약 삭제 시 저장되어있지 않은 아이디면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_not_saved_reservation_id() {
        assertThatThrownBy(() -> reservationService.deleteReservation(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 아이디입니다.");
    }

    @DisplayName("예약을 정상적으로 삭제한다.")
    @Test
    void success_delete_reservation() {
        Reservation reservation = new Reservation(1L, member, theme, date, time);
        memberRepository.insertMember(member);
        reservationTimeRepository.insertReservationTime(time);
        themeRepository.insertTheme(theme);
        reservationRepository.insertReservation(reservation);

        assertThatNoException()
                .isThrownBy(() -> reservationService.deleteReservation(reservation.getId()));
    }
}
