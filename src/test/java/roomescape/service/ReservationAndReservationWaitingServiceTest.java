package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.infrastructure.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.Fixture.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class ReservationAndReservationWaitingServiceTest {

    @Autowired
    private ReservationAndWaitingService reservationAndWaitingService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @Test
    @DisplayName("예약 대기가 존재하지 않는 경우 예약을 삭제한다.")
    void deleteReservationIfNoWaiting() {
        Member member = memberRepository.save(VALID_MEMBER);
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        String date = LocalDate.now().plusDays(2).toString();
        Reservation reservation = reservationRepository.save(new Reservation(member, new ReservationDate(date), time, theme));

        reservationAndWaitingService.deleteIfNoWaitingOrUpdateReservation(reservation.getId());

        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    @DisplayName("예약 대기가 존재하는 경우, 우선순위가 높은 대기를 예약으로 변경한다.")
    void updateReservation() {
        Member reservedMember = memberRepository.save(VALID_MEMBER);
        Member waitingMember = memberRepository.save(new Member(new MemberName("감자"),
                new MemberEmail("111@aaa.com"),
                VALID_USER_PASSWORD,
                MemberRole.USER));
        Theme theme = themeRepository.save(VALID_THEME);
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        ReservationDate date = new ReservationDate(LocalDate.now().plusDays(2).toString());
        Reservation reservation = reservationRepository.save(new Reservation(reservedMember, date, time, theme));
        ReservationWaiting waiting = reservationWaitingRepository.save(new ReservationWaiting(waitingMember, date, time, theme));

        reservationAndWaitingService.deleteIfNoWaitingOrUpdateReservation(reservation.getId());

        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertAll(
                () -> assertThat(reservationWaitingRepository.findById(waiting.getId())).isEmpty(),
                () -> assertThat(updatedReservation.getDate().getDate()).isEqualTo(date.getDate()),
                () -> assertThat(updatedReservation.getMember()).isEqualTo(waiting.getMember()),
                () -> assertThat(updatedReservation.getTheme().getId()).isEqualTo(theme.getId()),
                () -> assertThat(updatedReservation.getTime().getId()).isEqualTo(time.getId())
        );
    }
}
