package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.dto.request.ReservationTimeRequest;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.reservation.dto.AvailableReservationTimeDto;
import roomescape.exception.BadRequestException;
import roomescape.support.BaseServiceTest;

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
    @Sql("/available-reservation-times.sql")
    @DisplayName("이용 가능한 시간들을 조회한다.")
    void getAvailableReservationTimes() {
        LocalDate date = LocalDate.of(2024, 4, 9);
        Long themeId = 1L;

        List<AvailableReservationTimeDto> response = reservationTimeRepository
                .findAvailableReservationTimes(date, themeId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response).hasSize(4);

            softly.assertThat(response.get(0).id()).isEqualTo(1L);
            softly.assertThat(response.get(0).startAt()).isEqualTo("09:00");
            softly.assertThat(response.get(0).alreadyBooked()).isFalse();

            softly.assertThat(response.get(1).id()).isEqualTo(2L);
            softly.assertThat(response.get(1).startAt()).isEqualTo("12:00");
            softly.assertThat(response.get(1).alreadyBooked()).isTrue();

            softly.assertThat(response.get(2).id()).isEqualTo(3L);
            softly.assertThat(response.get(2).startAt()).isEqualTo("17:00");
            softly.assertThat(response.get(2).alreadyBooked()).isFalse();

            softly.assertThat(response.get(3).id()).isEqualTo(4L);
            softly.assertThat(response.get(3).startAt()).isEqualTo("21:00");
            softly.assertThat(response.get(3).alreadyBooked()).isTrue();
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
                .hasMessage("해당 id의 시간이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("예약 시간을 삭제할 때, 해당 예약 시간를 사용하는 예약이 존재하면 예외를 발생시킨다.")
    void deleteReservationTimeByIdFailWhenReservationExists() {
        Theme theme = themeRepository.save(new Theme("테마1", "테마 설명", "https://example.com"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));

        reservationRepository.save(Reservation.create(
                LocalDateTime.of(2024, 4, 6, 10, 30),
                LocalDate.of(2024, 4, 7),
                member,
                reservationTime,
                theme,
                ReservationStatus.RESERVED
        ));

        Long reservationTimeId = reservationTime.getId();

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(reservationTimeId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당 시간을 사용하는 예약이 존재합니다.");
    }
}
