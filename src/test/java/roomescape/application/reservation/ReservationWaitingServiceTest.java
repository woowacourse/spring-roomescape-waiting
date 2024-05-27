package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.ServiceTest;
import roomescape.application.reservation.dto.response.ReservationStatusResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberFixture;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;

@ServiceTest
class ReservationWaitingServiceTest {
    @Autowired
    private ReservationWaitingService reservationWaitingService;

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

    private LocalDate date;
    private ReservationTime time;
    private Theme theme;
    private Member member;
    private Reservation reservation;

    @BeforeEach
    void setData() {
        date = LocalDate.of(2024, 1, 1);
        time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        theme = themeRepository.save(new Theme("themeName", "desc", "url"));
        member = memberRepository.save(MemberFixture.createMember("아루"));
        reservation = reservationRepository.save(new Reservation(member, date, time, theme));
    }

    @Test
    @DisplayName("예약과 예약 대기를 모두 조회한다")
    void readAllReservationAndWaiting() {
        Member member1 = memberRepository.save(MemberFixture.createMember("시소"));
        waitingRepository.save(new Waiting(reservation, member1));

        Member member2 = memberRepository.save(MemberFixture.createMember("호돌"));
        waitingRepository.save(new Waiting(reservation, member2));

        List<ReservationStatusResponse> responses = reservationWaitingService.findAllByMemberId(member1.getId());
        assertThat(responses.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("대기가 없는 경우, 예약을 삭제한다.")
    void deleteByIdWhenNotExistWaiting() {
        reservationWaitingService.deleteById(member.getId(), reservation.getId());

        assertThat(reservationRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("대기가 있는 경우, 예약을 업데이트한다.")
    void deleteByIdWhenExistWaiting() {
        Member updatedMember = memberRepository.save(MemberFixture.createMember("시소"));
        waitingRepository.save(new Waiting(reservation, updatedMember));

        reservationWaitingService.deleteById(member.getId(), reservation.getId());

        List<Reservation> reservations = reservationRepository.findAll();
        assertAll(
                () -> assertThat(reservations.size()).isEqualTo(1),
                () -> assertThat(reservations.get(0).getMember().getName()).isEqualTo("시소"),
                () -> assertThat(waitingRepository.findAll().size()).isEqualTo(0)
        );
    }

}
