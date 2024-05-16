package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.exception.reservation.TimeDuplicatedException;
import roomescape.exception.reservation.TimeUsingException;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.reservation.ReservationTimeRequest;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@Import(ReservationTimeService.class)
@DataJpaTest
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("중복된 예약 시간을 생성하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_create_duplicated_reservation_time() {
        reservationTimeRepository.save(new ReservationTime("10:00"));

        ReservationTimeRequest requestDto = new ReservationTimeRequest("10:00");

        assertThatThrownBy(() -> reservationTimeService.createReservationTime(requestDto))
                .isInstanceOf(TimeDuplicatedException.class)
                .hasMessage("중복된 시간을 입력할 수 없습니다.");
    }

    @DisplayName("예약 시간 생성에 성공한다.")
    @Test
    void success_create_reservation_time() {
        ReservationTimeRequest requestDto = new ReservationTimeRequest("10:00");

        assertThatNoException()
                .isThrownBy(() -> reservationTimeService.createReservationTime(requestDto));
    }

    @DisplayName("예약 시간 삭제 시 해당 시간에 예약이 존재하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_delete_reservation_time_with_existing_reservation() {
        Member member = new Member("t1@t1.com", "123", "러너덕", "MEMBER");
        Theme theme = new Theme("공포", "공포는 무서워", "hi.jpg");
        LocalDate date = LocalDate.parse("2025-11-30");
        ReservationTime time = new ReservationTime("11:00");
        Reservation reservation = new Reservation(member, theme, date, time);

        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
                .isInstanceOf(TimeUsingException.class)
                .hasMessage("해당 시간에 예약이 있어 삭제할 수 없습니다.");
    }

    @DisplayName("예약 시간 삭제에 성공한다.")
    @Test
    void success_delete_reservation_time() {
        ReservationTime time = new ReservationTime("11:00");
        reservationTimeRepository.save(time);

        assertThatNoException()
                .isThrownBy(() -> reservationTimeService.deleteReservationTime(1L));
    }
}
