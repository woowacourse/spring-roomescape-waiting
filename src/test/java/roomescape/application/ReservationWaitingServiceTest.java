package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.response.MyReservationResponse;
import roomescape.application.dto.response.ReservationStatus;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.fixture.Fixture;

class ReservationWaitingServiceTest extends BaseServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Test
    @DisplayName("회원 아이디로 예약과 예약 대기들을 예약 대기 순번을 포함해서 조회한다.")
    void getMyReservationWithRanks() {
        // given
        LocalDate date = LocalDate.of(2024, 4, 6);

        Member member1 = memberRepository.save(Fixture.MEMBER_1);
        Member member2 = memberRepository.save(Fixture.MEMBER_2);

        Theme theme1 = themeRepository.save(Fixture.THEME_1);

        ReservationTime time1 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_1);
        ReservationTime time2 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_2);

        Reservation reservation = reservationRepository
                .save(new Reservation(new ReservationDetail(date, time1, theme1), member1));
        reservationRepository.save(new Reservation(new ReservationDetail(date, time1, theme1), member2));
        Waiting waiting = waitingRepository.save(new Waiting(new ReservationDetail(date, time2, theme1), member1));

        // when
        List<MyReservationResponse> responses = reservationWaitingService
                .getMyReservationAndWaitingWithRanks(member1.getId());

        // then2
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(2);

            softly.assertThat(responses.get(0).id()).isEqualTo(reservation.getId());
            softly.assertThat(responses.get(0).rank()).isEqualTo(0);
            softly.assertThat(responses.get(0).status()).isEqualTo(ReservationStatus.RESERVED);

            softly.assertThat(responses.get(1).id()).isEqualTo(waiting.getId());
            softly.assertThat(responses.get(1).rank()).isEqualTo(1);
            softly.assertThat(responses.get(1).status()).isEqualTo(ReservationStatus.WAITING);
        });
    }
}
