package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.repository.*;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.output.WaitingOutput;
import roomescape.util.DatabaseCleaner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class WaitingServiceTest {
    @Autowired
    DatabaseCleaner databaseCleaner;
    @Autowired
    WaitingService sut;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationInfoRepository reservationInfoRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    WaitingRepository waitingRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.initialize();

    }

    @Test
    @DisplayName("예약이 없는 정보에 대해서는 대기할 수 없다.")
    void some() {
        final ReservationTime time = reservationTimeRepository.save(ReservationTime.from("11:00"));
        final Theme theme = themeRepository.save(ThemeFixture.getDomain());
        final Member member = memberRepository.save(MemberFixture.getDomain());
        final ReservationInput input = new ReservationInput("2025-01-01", time.getId(), theme.getId(), member.getId());

        assertThatThrownBy(() -> sut.createWaiting(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("대기는 1번부터 시작한다.")
    void order_start_one() {
        final ReservationTime time = reservationTimeRepository.save(ReservationTime.from("11:00"));
        final Theme theme = themeRepository.save(ThemeFixture.getDomain());
        final Member member = memberRepository.save(MemberFixture.getDomain());

        final ReservationInfo reservationInfo = reservationInfoRepository.save(ReservationInfo.from("2025-01-01", time, theme));
        reservationRepository.save(new Reservation(member, reservationInfo));

        final WaitingOutput output = sut.createWaiting(new ReservationInput("2025-01-01", time.getId(), theme.getId(), member.getId()));

        assertThat(output.order()).isOne();
    }

    @Test
    @DisplayName("같은 예약 정보로 똑같은 사용자가 두 번 대기 신청하면 예외를 발생한다.")
    void throw_exception_when_same_user_with_same_reservationInfo() {
        final ReservationTime time = reservationTimeRepository.save(ReservationTime.from("11:00"));
        final Theme theme = themeRepository.save(ThemeFixture.getDomain());
        final Member member = memberRepository.save(MemberFixture.getDomain());

        final ReservationInfo reservationInfo = reservationInfoRepository.save(ReservationInfo.from("2025-01-01", time, theme));
        reservationRepository.save(new Reservation(member, reservationInfo));
        final ReservationInput input = new ReservationInput("2025-01-01", time.getId(), theme.getId(), member.getId());

        sut.createWaiting(input);

        assertThatThrownBy(() -> sut.createWaiting(input))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    @DisplayName("대기는 생성된 순으로 순서가 결정된다.")
    void order_decide_orderBy_createdAt() {
        final ReservationTime time = reservationTimeRepository.save(ReservationTime.from("11:00"));
        final Theme theme = themeRepository.save(ThemeFixture.getDomain());
        final Member member1 = memberRepository.save(MemberFixture.getDomain("alphaka@gmail.com"));
        final Member member2 = memberRepository.save(MemberFixture.getDomain("joyson5582@gmail.com"));
        final Member member3 = memberRepository.save(MemberFixture.getDomain("brown@gmail.com"));
        final ReservationInfo reservationInfo = reservationInfoRepository.save(ReservationInfo.from("2025-01-01", time, theme));
        reservationRepository.save(new Reservation(member3, reservationInfo));

        final WaitingOutput output1 = sut.createWaiting(new ReservationInput("2025-01-01", time.getId(), theme.getId(), member1.getId()));
        final WaitingOutput output2 = sut.createWaiting(new ReservationInput("2025-01-01", time.getId(), theme.getId(), member2.getId()));
        assertAll(() -> {
            assertThat(output1.order()).isEqualTo(1);
            assertThat(output2.order()).isEqualTo(2);
        });
    }
}
