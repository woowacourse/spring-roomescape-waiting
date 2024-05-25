package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.CreateReservationTimeRequest;
import roomescape.reservation.dto.response.CreateReservationTimeResponse;
import roomescape.reservation.dto.response.FindReservationTimeResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
public class ReservationTimeServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.deleteAll();
        reservationRepository.deleteAll();
    }

    @Test
    @DisplayName("방탈출 시간대 생성 성공 시, 생성된 시간대의 정보를 반환한다.")
    void createReservationTime() {
        CreateReservationTimeRequest request = new CreateReservationTimeRequest(LocalTime.of(10, 0));

        CreateReservationTimeResponse response = reservationTimeService.createReservationTime(request);

        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0))
        );
    }

    @Test
    @DisplayName("방탈출 시간대 생성 시, 이미 존재하는 시간인 경우 예외를 반환한다.")
    void createReservationTime_WhenTimeIsExist() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        CreateReservationTimeRequest request = new CreateReservationTimeRequest(LocalTime.of(10, 0));

        assertThatThrownBy(() -> reservationTimeService.createReservationTime(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("생성하려는 시간 10:00가 이미 존재합니다. 시간을 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("방탈출 시간대 목록을 조회한다.")
    void getReservationTimes() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(20, 0)));

        List<FindReservationTimeResponse> response = reservationTimeService.getReservationTimes();

        assertAll(
                () -> assertThat(response).hasSize(2),
                () -> assertThat(response).extracting("startAt")
                        .containsExactlyInAnyOrder(LocalTime.parse("10:00"), LocalTime.parse("20:00"))
        );
    }

    @Test
    @DisplayName("방탈출 시간대를 조회한다.")
    void getReservationTime() {
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        FindReservationTimeResponse response = reservationTimeService.getReservationTime(savedTime.getId());

        assertAll(
                () -> assertThat(response.id()).isEqualTo(savedTime.getId()),
                () -> assertThat(response.startAt()).isEqualTo("10:00")
        );
    }

    @Test
    @DisplayName("방탈출 시간 조회 시, 조회하려는 시간이 없는 경우 예외를 반환한다.")
    void getReservationTime_WhenTimeNotExist() {
        assertThatThrownBy(() -> reservationTimeService.getReservationTime(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("해당하는 예약이 존재하지 않아 시간을 조회할 수 없습니다.");
    }

    @Test
    @DisplayName("방탈출 시간대를 삭제한다.")
    void deleteReservationTime() {
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        reservationTimeService.deleteById(savedTime.getId());

        Optional<ReservationTime> deletedTime = reservationTimeRepository.findById(savedTime.getId());
        assertThat(deletedTime).isEmpty();
    }

    @Test
    @DisplayName("방탈출 시간 삭제 시, 삭제하려는 시간이 존재하지 않는 경우 예외를 반환한다.")
    void deleteReservationTime_WhenTimeNotExist() {
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("식별자 1에 해당하는 시간이 존재하지 않습니다. 삭제가 불가능합니다.");
    }

    @Test
    @DisplayName("방탈출 시간 삭제 시, 사용 중인 시간인 경우 예외를 반환한다.")
    void deleteReservationTime_WhenTimeInUsage() {
        Member member = memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));

        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        reservationRepository.save(new Reservation(member, LocalDate.now(), savedTime, theme));

        assertThatThrownBy(() -> reservationTimeService.deleteById(savedTime.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("시간을 사용 중인 예약이 존재합니다. 삭제가 불가능합니다.");
    }
}
