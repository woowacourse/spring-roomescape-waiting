package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.infrastructure.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
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

    private Member member;
    private Theme theme;
    private ReservationTime time;
    private ReservationDate date;

    @BeforeEach
    void init() {
        member = memberRepository.save(VALID_MEMBER);
        theme = themeRepository.save(VALID_THEME);
        time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        date = new ReservationDate(LocalDate.now().plusDays(2).toString());
    }

    @Test
    @DisplayName("예약 대기가 존재하지 않는 경우 예약을 삭제한다.")
    void deleteReservationIfNoWaiting() {
        Reservation reservation = reservationRepository.save(new Reservation(member, date, time, theme));

        reservationAndWaitingService.deleteReservation(reservation.getId());

        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    @DisplayName("예약 대기가 존재하는 경우, 우선순위가 높은 대기를 예약으로 변경한다.")
    void updateReservation() {
        Member waitingMember = memberRepository.save(new Member(new MemberName("감자"), new MemberEmail("111@aaa.com"), VALID_USER_PASSWORD, MemberRole.USER));
        LocalDateTime createdDateTime = LocalDateTime.now().minusMonths(2);
        Reservation reservation = reservationRepository.save(new Reservation(member, date, time, theme));
        ReservationWaiting waiting = reservationWaitingRepository.save(new ReservationWaiting(createdDateTime, waitingMember, date, time, theme));

        reservationAndWaitingService.deleteReservation(reservation.getId());

        Reservation updatedReservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                        reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId())
                .orElseThrow();

        assertThat(updatedReservation.getMember()).isEqualTo(waiting.getMember());
    }
}
