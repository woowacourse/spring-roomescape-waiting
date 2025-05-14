package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.fake.FakeMemberRepository;
import roomescape.repository.fake.FakeReservationRepository;
import roomescape.repository.fake.FakeReservationTimeRepository;
import roomescape.repository.fake.FakeThemeRepository;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;

class ReservationServiceTest {

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        ReservationRepository reservationRepository = new FakeReservationRepository();
        MemberRepository memberRepository = new FakeMemberRepository();
        ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
        ThemeRepository themeRepository = new FakeThemeRepository();
        reservationService = new ReservationService(reservationRepository, memberRepository,
            reservationTimeRepository, themeRepository);

        // 기본 예약 시간, 테마 추가
        memberRepository.addMember(new Member(1L, "사나", "이메일", "비밀번호"));
        reservationTimeRepository.addTime(new ReservationTime(1L, LocalTime.of(10, 0)));
        themeRepository.addTheme(new Theme(1L, "테마", "설명", "썸네일"));
    }

    @Test
    @DisplayName("모든 예약 정보를 가져올 수 있다.")
    void findAllReservations() {
        ReservationRequest request1 = new ReservationRequest(
            LocalDate.of(2024, 4, 26), 1L, 1L, 1L);
        ReservationRequest request2 = new ReservationRequest(
            LocalDate.of(2024, 4, 28), 1L, 1L, 1L);

        reservationService.addReservation(request1);
        reservationService.addReservation(request2);

        assertThat(reservationService.findAllReservations()).hasSize(2);
    }

    @Test
    @DisplayName("예약 추가를 할 수 있다.")
    void addReservation() {
        ReservationRequest request = new ReservationRequest(
            LocalDate.of(2024, 4, 26), 1L, 1L, 1L);
        Reservation actual = reservationService.addReservation(request);

        assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getMember().getId()).isEqualTo(1L);
            assertThat(actual.getDate()).isEqualTo(LocalDate.of(2024, 4, 26));
            assertThat(actual.getTime().getId()).isEqualTo(1L);
            assertThat(actual.getTheme().getId()).isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("예약이 이미 존재한다면 추가를 할 수 없다.")
    void addDuplicatedReservation() {
        ReservationRequest request = new ReservationRequest(
            LocalDate.of(2024, 4, 26), 1L, 1L, 1L);
        reservationService.addReservation(request);

        assertThatThrownBy(() -> reservationService.addReservation(request))
            .isInstanceOf(DuplicatedException.class)
            .hasMessageContaining("reservation");
    }

    @Test
    @DisplayName("과거의 예약은 불가능하다.")
    void addReservationBeforeNow() {
        Member adminMember = new Member("admin", "admin", "1234");
        ReservationRequest request = new ReservationRequest(
            LocalDate.of(2024, 4, 26), 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservationAfterNow(adminMember, request))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("과거 예약은 불가능");
    }

    @Test
    @DisplayName("예약을 id를 통해 제거할 수 있다.")
    void removeReservation() {
        ReservationRequest request = new ReservationRequest(
            LocalDate.of(2024, 4, 26), 1L, 1L, 1L);
        reservationService.addReservation(request);

        reservationService.removeReservation(1L);

        assertThat(reservationService.findAllReservations()).hasSize(0);
    }
}
