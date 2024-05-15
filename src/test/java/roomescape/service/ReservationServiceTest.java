package roomescape.service;


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
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.exception.DateTimePassedException;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.TimeNotFoundException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    private JpaReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JpaThemeRepository themeRepository;

    @Autowired
    private JpaReservationRepository reservationRepository;

    @Autowired
    private JpaMemberRepository memberRepository;

    private final Member member = new Member(1L, "t1@t1.com", "123", "러너덕", "MEMBER");
    private final ReservationTime time = new ReservationTime(1L, "11:00");
    private final Theme theme = new Theme(1L, "공포", "공포는 무서워", "hi.jpg");
    private final LocalDate date = LocalDate.parse("2025-11-30");

    @DisplayName("저장되어있지 않은 예약 시간에 예약을 시도하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_unsaved_time() {
        memberRepository.save(member);
        themeRepository.save(theme);

        ReservationCreate reservationDto = new ReservationCreate(1L, 1L, "2025-11-30", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(TimeNotFoundException.class)
                .hasMessage("예약 하려는 시간이 저장되어 있지 않습니다.");
    }

    @DisplayName("이미 지나간 날짜에 예약을 시도하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_before_date() {
        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);

        ReservationCreate reservationDto = new ReservationCreate(1L, 1L, "2024-05-07", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(DateTimePassedException.class)
                .hasMessage("지나간 날짜와 시간에 대한 예약은 불가능합니다.");
    }

    @DisplayName("같은 테마를 같은 시간에 예약을 시도하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_create_reservation_use_same_theme_and_date_time() {
        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        Reservation reservation1 = new Reservation(member, theme, date, time);
        reservationRepository.save(reservation1);

        ReservationCreate reservationDto = new ReservationCreate(1L, 1L, "2025-11-30", 1L);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDto))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("해당 테마는 같은 시간에 이미 예약이 존재합니다.");
    }

    @DisplayName("예약을 정상적으로 생성한다.")
    @Test
    void success_create_reservation() {
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        memberRepository.save(member);

        ReservationCreate reservationDto = new ReservationCreate(1L, 1L, "2025-11-30", 1L);

        assertThatNoException()
                .isThrownBy(() -> reservationService.createReservation(reservationDto));
    }


    @DisplayName("예약 삭제 시 저장되어있지 않은 아이디면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_not_saved_reservation_id() {
        assertThatThrownBy(() -> reservationService.deleteReservation(1L))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessage("존재하지 않는 아이디입니다.");
    }

    @DisplayName("예약을 정상적으로 삭제한다.")
    @Test
    void success_delete_reservation() {
        Reservation reservation = new Reservation(member, theme, date, time);
        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        reservationRepository.save(reservation);

        assertThatNoException()
                .isThrownBy(() -> reservationService.deleteReservation(reservation.getId()));
    }
}
