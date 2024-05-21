package roomescape.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.getMemberChoco;
import static roomescape.fixture.MemberFixture.getMemberClover;
import static roomescape.fixture.ReservationFixture.getNextDayReservation;
import static roomescape.fixture.ReservationTimeFixture.get1PM;
import static roomescape.fixture.ReservationTimeFixture.getNoon;
import static roomescape.fixture.ThemeFixture.getTheme1;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.util.ServiceTest;
import roomescape.waiting.domain.WaitingUpdateHistory;
import roomescape.waiting.domain.repository.WaitingUpdateHistoryRepository;

@DisplayName("웨이팅 히스토리 로직 테스트")
class WaitingHistoryServiceTest extends ServiceTest {
    @Autowired
    WaitingUpdateHistoryRepository waitingUpdateHistoryRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    WaitingHistoryService waitingHistoryService;

    ReservationTime timeNoon;
    Theme theme1;
    Member memberChoco;

    @BeforeEach
    void setUp() {
        timeNoon = reservationTimeRepository.save(getNoon());
        theme1 = themeRepository.save(getTheme1());
        memberChoco = memberRepository.save(getMemberChoco());
    }

    @DisplayName("히스토리 생성에 성공한다.")
    @Test
    void create() {
        //given
        Reservation reservation = reservationRepository.save(getNextDayReservation(timeNoon, theme1));

        //when
        waitingHistoryService.createHistory(reservation);

        //then
        assertThat(waitingUpdateHistoryRepository.findAll()).hasSize(1);
    }
}
