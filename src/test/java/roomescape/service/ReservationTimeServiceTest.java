package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.service.dto.request.CreateReservationTimeRequest;
import roomescape.service.dto.response.AvailableReservationTimeResponse;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.support.fixture.MemberFixture;
import roomescape.support.fixture.ReservationFixture;
import roomescape.support.fixture.ReservationTimeFixture;
import roomescape.support.fixture.ThemeFixture;

class ReservationTimeServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("모든 예약 시간들을 조회한다.")
    void getAllReservationTimes() {
        ReservationTime nine = reservationTimeRepository.save(ReservationTimeFixture.create("09:00"));
        ReservationTime ten = reservationTimeRepository.save(ReservationTimeFixture.create("10:00"));

        List<ReservationTimeResponse> responses = reservationTimeService.getAllReservationTimes();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(2);
            softly.assertThat(responses.get(0).startAt()).isEqualTo(nine.getStartAt());
            softly.assertThat(responses.get(1).startAt()).isEqualTo(ten.getStartAt());
        });
    }

    @Test
    @DisplayName("예약 시간을 추가한다.")
    void addReservationTime() {
        CreateReservationTimeRequest request = new CreateReservationTimeRequest(LocalTime.of(10, 30));
        ReservationTimeResponse response = reservationTimeService.addReservationTime(request);

        assertThat(response.startAt()).isEqualTo("10:30");
    }

    @Test
    @DisplayName("id로 예약 시간을 삭제한다.")
    void deleteReservationTimeById() {
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.ten());
        long id = reservationTime.getId();

        reservationTimeService.deleteReservationTimeById(id);

        assertThat(reservationTimeRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("날짜와 테마 id로 예약 가능한 시간들을 조회한다.")
    void getAvailableReservationTimes() {
        Member member = memberRepository.save(MemberFixture.user());
        Theme theme = themeRepository.save(ThemeFixture.theme());
        ReservationTime nine = reservationTimeRepository.save(ReservationTimeFixture.create("09:00"));
        ReservationTime ten = reservationTimeRepository.save(ReservationTimeFixture.create("10:00"));
        ReservationTime eleven = reservationTimeRepository.save(ReservationTimeFixture.create("11:00"));
        ReservationTime twelve = reservationTimeRepository.save(ReservationTimeFixture.create("12:00"));
        String date = "2024-04-09";
        reservationRepository.save(ReservationFixture.create(date, member, ten, theme));
        reservationRepository.save(ReservationFixture.create(date, member, twelve, theme));

        List<AvailableReservationTimeResponse> response = reservationTimeService
                .getAvailableReservationTimes(LocalDate.parse(date), theme.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response).hasSize(4);

            softly.assertThat(response.get(0).timeId()).isEqualTo(1L);
            softly.assertThat(response.get(0).startAt()).isEqualTo(nine.getStartAt());
            softly.assertThat(response.get(0).alreadyBooked()).isFalse();

            softly.assertThat(response.get(1).timeId()).isEqualTo(2L);
            softly.assertThat(response.get(1).startAt()).isEqualTo(ten.getStartAt());
            softly.assertThat(response.get(1).alreadyBooked()).isTrue();

            softly.assertThat(response.get(2).timeId()).isEqualTo(3L);
            softly.assertThat(response.get(2).startAt()).isEqualTo(eleven.getStartAt());
            softly.assertThat(response.get(2).alreadyBooked()).isFalse();

            softly.assertThat(response.get(3).timeId()).isEqualTo(4L);
            softly.assertThat(response.get(3).startAt()).isEqualTo(twelve.getStartAt());
            softly.assertThat(response.get(3).alreadyBooked()).isTrue();
        });
    }
}
