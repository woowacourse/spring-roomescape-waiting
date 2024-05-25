package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.Fixture.DATE_1;
import static roomescape.fixture.Fixture.MEMBER_1;
import static roomescape.fixture.Fixture.RESERVATION_TIME_1;
import static roomescape.fixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.domain.reservation.dto.WaitingWithRankDto;
import roomescape.fixture.Fixture;

@DataJpaTest
class WaitingRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("회원 아이디로 예약 대기 순번을 포함한 예약 대기들을 조회한다.")
    void findWaitingsWithRankByMemberId() {
        // given
        LocalDate date = LocalDate.of(2024, 4, 6);

        Member member1 = memberRepository.save(Fixture.MEMBER_1);
        Member member2 = memberRepository.save(Fixture.MEMBER_2);

        Theme theme1 = themeRepository.save(Fixture.THEME_1);

        ReservationTime time1 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_1);
        ReservationTime time2 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_2);

        waitingRepository.save(new Waiting(new ReservationDetail(date, time1, theme1), member2));
        Waiting waiting1 = waitingRepository.save(new Waiting(new ReservationDetail(date, time1, theme1), member1));
        Waiting waiting2 = waitingRepository.save(new Waiting(new ReservationDetail(date, time2, theme1), member1));

        // when
        List<WaitingWithRankDto> waitingWithRankDtos = waitingRepository
                .findWaitingsWithRankByMemberId(member1.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(waitingWithRankDtos).hasSize(2);

            softly.assertThat(waitingWithRankDtos.get(0).waiting().getId()).isEqualTo(waiting1.getId());
            softly.assertThat(waitingWithRankDtos.get(0).rank()).isEqualTo(2);

            softly.assertThat(waitingWithRankDtos.get(1).waiting().getId()).isEqualTo(waiting2.getId());
            softly.assertThat(waitingWithRankDtos.get(1).rank()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("아아디로 예약 대기을 조회한다.")
    void getById() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_1);
        Theme theme = themeRepository.save(THEME_1);
        Member member = memberRepository.save(MEMBER_1);
        ReservationDetail detail = new ReservationDetail(DATE_1, reservationTime, theme);

        Waiting savedWaiting = waitingRepository.save(new Waiting(detail, member));

        // when
        Waiting waiting = waitingRepository.getById(savedWaiting.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(waiting.getId()).isEqualTo(savedWaiting.getId());
            softly.assertThat(waiting.getDetail()).isEqualTo(detail);
            softly.assertThat(waiting.getMember()).isEqualTo(member);
        });
    }

    @Test
    @DisplayName("아이디로 예약 대기을 조회하고, 없을 경우 예외를 발생시킨다.")
    void getByIdWhenNotExist() {
        assertThatThrownBy(() -> waitingRepository.getById(-1L))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessage(String.format("해당 id의 예약 대기가 존재하지 않습니다. (id: %d)", -1L));
    }
}
