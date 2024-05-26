package roomescape.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.reservation.ReservationStatus.RESERVED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.CreateTimeResponse;
import roomescape.controller.dto.FindTimeAndAvailabilityResponse;
import roomescape.controller.dto.FindTimeResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private final String rawTime = "19:00";

    @DisplayName("성공: 예약 시간을 저장하고, id 값과 함께 반환한다.")
    @Test
    void save() {
        CreateTimeResponse saved = reservationTimeService.save(rawTime);
        assertThat(saved.id()).isEqualTo(1L);
    }

    @DisplayName("실패: 이미 존재하는 시간을 추가할 수 없다.")
    @Test
    void save_TimeAlreadyExists() {
        reservationTimeRepository.save(new ReservationTime(rawTime));

        assertThatThrownBy(() -> reservationTimeService.save(rawTime))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 존재하는 시간은 추가할 수 없습니다.");
    }

    @DisplayName("성공: 예약 시간 삭제")
    @Test
    void delete() {
        reservationTimeRepository.save(new ReservationTime("10:00"));
        reservationTimeRepository.save(new ReservationTime("11:00"));
        reservationTimeRepository.save(new ReservationTime("12:00"));

        reservationTimeService.delete(2L);

        assertThat(reservationTimeRepository.findAll())
            .extracting(ReservationTime::getId)
            .containsExactly(1L, 3L);
    }

    @DisplayName("실패: 시간을 사용하는 예약이 존재하는 경우 시간을 삭제할 수 없다.")
    @Test
    void delete_ReservationExists() {
        Member member = memberRepository.save(new Member("러너덕", "deock@test.com", "123a!", Role.USER));
        Theme theme = themeRepository.save(new Theme("테마1", "설명1", "https://test.com/test1.jpg"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime("10:00"));
        reservationRepository.save(new Reservation(
            member, LocalDate.parse("2060-01-01"), LocalDateTime.now(), time, theme, RESERVED));

        assertThatThrownBy(() -> reservationTimeService.delete(1L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 시간을 사용하는 예약이 존재하여 삭제할 수 없습니다.");
    }

    @DisplayName("성공: 전체 예약시간 조회")
    @Test
    void findAll() {
        reservationTimeRepository.save(new ReservationTime("10:00"));
        reservationTimeRepository.save(new ReservationTime("11:00"));
        reservationTimeRepository.save(new ReservationTime("12:00"));

        assertThat(reservationTimeService.findAll())
            .extracting(FindTimeResponse::id)
            .containsExactly(1L, 2L, 3L);
    }

    @DisplayName("성공: 전체 예약시간 및 예약 가능 여부 조회")
    @Test
    void findAllWithBookAvailability() {
        LocalDate date = LocalDate.parse("2060-01-01");

        Member member = memberRepository.save(new Member("러너덕", "deock@test.com", "123a!", Role.USER));
        Theme theme = themeRepository.save(new Theme("테마1", "설명1", "https://test.com/test.jpg"));
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime("10:00"));
        reservationTimeRepository.save(new ReservationTime("11:00"));
        ReservationTime time3 = reservationTimeRepository.save(new ReservationTime("12:00"));

        reservationRepository.save(new Reservation(member, date, LocalDateTime.now(), time1, theme, RESERVED));
        reservationRepository.save(new Reservation(member, date, LocalDateTime.now(), time3, theme, RESERVED));

        List<FindTimeAndAvailabilityResponse> response =
            reservationTimeService.findAllWithBookAvailability(date, 1L);

        Assertions.assertAll(
            () -> assertThat(response)
                .extracting(FindTimeAndAvailabilityResponse::id)
                .containsExactly(1L, 2L, 3L),
            () -> assertThat(response)
                .extracting(FindTimeAndAvailabilityResponse::alreadyBooked)
                .containsExactly(true, false, true)
        );
    }
}
