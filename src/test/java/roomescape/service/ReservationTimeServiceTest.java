package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.MEMBER1;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.RESERVATION_TIME_11AM;
import static roomescape.TestFixture.THEME1;
import static roomescape.TestFixture.TIME_10AM;
import static roomescape.TestFixture.TOMORROW;

import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.exception.BadRequestException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ReservationAvailabilityTimeRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.response.ReservationAvailabilityTimeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
        reservationTimeRepository.deleteAllInBatch();
        themeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("중복된 예약 시간을 저장하려 하면 예외가 발생한다.")
    @Test
    void duplicatedTimeSaveThrowsException() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(TIME_10AM);
        reservationTimeService.save(reservationTimeRequest);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.save(reservationTimeRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("중복된 시간을 생성할 수 없습니다.");
    }

    @DisplayName("특정 날짜, 테마에 대해 예약 가능한 시간을 조회한다.")
    @Test
    void findReservationAvailabilityTimes() {
        // given - 같은 날짜, 테마에 대해 10시는 예약, 11시는 예약하지 않는다.
        Theme theme = themeRepository.save(THEME1);
        Member member = memberRepository.save(MEMBER1);

        ReservationTime time1 = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        ReservationTime time2 = reservationTimeRepository.save(RESERVATION_TIME_11AM);

        reservationRepository.save(new Reservation(member, TOMORROW, time2, theme, Status.CONFIRMED));

        // when
        ReservationAvailabilityTimeRequest timeRequest = new ReservationAvailabilityTimeRequest(
                TOMORROW, theme.getId());
        List<ReservationAvailabilityTimeResponse> timeResponses =
                reservationTimeService.findReservationAvailabilityTimes(timeRequest).responses();

        // then
        assertThat(timeResponses).extracting("booked").contains(false, true);
        assertThat(timeResponses).extracting("id").containsExactly(time1.getId(), time2.getId());

    }
}
