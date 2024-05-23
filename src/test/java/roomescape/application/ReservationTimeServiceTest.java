package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.Fixture.MEMBER_1;
import static roomescape.fixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.request.ReservationTimeRequest;
import roomescape.application.dto.response.AvailableReservationTimeResponse;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.exception.BadRequestException;

class ReservationTimeServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약 시간을 추가한다.")
    void addReservationTime() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 30));
        ReservationTimeResponse response = reservationTimeService.addReservationTime(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response).isNotNull();
            softly.assertThat(response.startAt()).isEqualTo("10:30");
        });
    }

    @Test
    @DisplayName("모든 예약 시간들을 조회한다.")
    void getAllReservationTimes() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 30)));

        List<ReservationTimeResponse> responses = reservationTimeService.getAllReservationTimes();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(2);
            softly.assertThat(responses.get(0).startAt()).isEqualTo("10:30");
            softly.assertThat(responses.get(1).startAt()).isEqualTo("11:30");
        });
    }

    @Test
    @DisplayName("이용 가능한 시간들을 조회한다.")
    void getAvailableReservationTimes() {
        // given
        LocalDateTime now = LocalDateTime.of(2024, 4, 8, 10, 0);
        LocalDate date = LocalDate.of(2024, 4, 10);
        Theme theme = themeRepository.save(THEME_1);

        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        ReservationDetail detail = new ReservationDetail(date, time1, theme);

        Member member = memberRepository.save(MEMBER_1);

        reservationRepository.save(Reservation.create(now, detail, member));

        // when
        List<AvailableReservationTimeResponse> responses = reservationTimeService
                .getAvailableReservationTimes(date, theme.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(2);

            softly.assertThat(responses.get(0).timeId()).isEqualTo(time1.getId());
            softly.assertThat(responses.get(0).startAt()).isEqualTo("09:00");
            softly.assertThat(responses.get(0).alreadyBooked()).isTrue();

            softly.assertThat(responses.get(1).timeId()).isEqualTo(time2.getId());
            softly.assertThat(responses.get(1).startAt()).isEqualTo("10:00");
            softly.assertThat(responses.get(1).alreadyBooked()).isFalse();
        });
    }

    @Test
    @DisplayName("id로 예약 시간을 삭제한다.")
    void deleteReservationTimeById() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));

        reservationTimeService.deleteReservationTimeById(reservationTime.getId());

        assertThat(reservationTimeRepository.findById(reservationTime.getId())).isEmpty();
    }

    @Test
    @DisplayName("예약 시간을 삭제할 때, 해당 id의 예약 시간이 존재하지 않으면 예외를 발생시킨다.")
    void deleteReservationTimeByIdFailWhenReservationTimeNotFound() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(-1L))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessage(String.format("해당 id의 예약 시간이 존재하지 않습니다. (id: %d)", -1L));
    }

    @Test
    @DisplayName("예약 시간을 삭제할 때, 해당 예약 시간를 사용하는 예약이 존재하면 예외를 발생시킨다.")
    void deleteReservationTimeByIdFailWhenReservationExists() {
        Theme theme = themeRepository.save(new Theme("테마1", "테마 설명", "https://example.com"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));

        LocalDateTime now = LocalDateTime.of(2024, 4, 6, 10, 30);
        LocalDate reservationDate = LocalDate.of(2024, 4, 8);
        ReservationDetail detail = new ReservationDetail(reservationDate, reservationTime, theme);

        reservationRepository.save(Reservation.create(now, detail, member));

        Long reservationTimeId = reservationTime.getId();

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(reservationTimeId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(String.format("해당 예약 시간을 사용하는 예약이 존재합니다. (예약 시간 id: %d)", reservationTimeId));
    }
}
