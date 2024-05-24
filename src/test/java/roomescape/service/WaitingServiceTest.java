package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.waiting.WaitingResponse;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@Import(WaitingService.class)
@DataJpaTest
class WaitingServiceTest {

    private final Member sampleMember = new Member("t1@t1.com", "123", "돌안", "MEMBER");
    private final ReservationTime sampleTime = new ReservationTime("11:00");
    private final Theme sampleTheme = new Theme("공포", "공포는 무서워", "hi.jpg");
    private final LocalDate sampleDate = LocalDate.parse("2025-11-30");

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private WaitingService waitingService;

    @Test
    @DisplayName("이미 예약된 것에 대기를 생성한다.")
    void createWaiting() {
        // given
        Member member = memberRepository.save(sampleMember);
        ReservationTime time = reservationTimeRepository.save(sampleTime);
        Theme theme = themeRepository.save(sampleTheme);
        Reservation reservation = reservationRepository.save(new Reservation(member, theme, sampleDate, time));

        // when
        WaitingResponse actual = waitingService.createWaiting(new ReservationCreate(
                member.getEmail(), theme.getId(), reservation.getDate().toString(), time.getId()
        ));

        // then
        assertThat(actual.reservationId()).isEqualTo(reservation.getId());
    }

    @Test
    @DisplayName("존재하지 않는 예약에 대한 대기를 생성할 시 예외가 발생한다.")
    void createWaitingNotExistReservation() {
        // given
        Member member = memberRepository.save(sampleMember);
        ReservationTime time = reservationTimeRepository.save(sampleTime);
        Theme theme = themeRepository.save(sampleTheme);
        Reservation reservation = reservationRepository.save(new Reservation(member, theme, sampleDate, time));
        reservationRepository.delete(reservation);

        ReservationCreate request = new ReservationCreate(member.getEmail(), theme.getId(),
                reservation.getDate().toString(),
                time.getId());

        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 멤버에 대한 대기를 생성할 시 예외가 발생한다.")
    void createWaitingNotExistMember() {
        // given
        Member member = memberRepository.save(sampleMember);
        ReservationTime time = reservationTimeRepository.save(sampleTime);
        Theme theme = themeRepository.save(sampleTheme);
        Reservation reservation = reservationRepository.save(new Reservation(member, theme, sampleDate, time));
        String notExistEmail = "no@test.com";

        ReservationCreate request = new ReservationCreate(notExistEmail, theme.getId(),
                reservation.getDate().toString(),
                time.getId());

        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(MemberNotFoundException.class);
    }
}
