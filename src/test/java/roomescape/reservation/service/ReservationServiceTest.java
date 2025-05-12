package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.dao.MemberDao;
import roomescape.member.model.Member;
import roomescape.member.service.fake.FakeMemberDao;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.dao.ReservationTimeDao;
import roomescape.reservation.dao.ThemeDao;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.NotCorrectDateTimeException;
import roomescape.reservation.service.fake.FakeReservationDao;
import roomescape.reservation.service.fake.FakeReservationTimeDao;
import roomescape.reservation.service.fake.FakeThemeDao;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(ReservationService.class)
public class ReservationServiceTest {

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        ReservationDao reservationDao = new FakeReservationDao();
        ReservationTimeDao reservationTimeDao = new FakeReservationTimeDao();
        ThemeDao themeDao = new FakeThemeDao();
        MemberDao memberDao = new FakeMemberDao();
        reservationService = new ReservationService(reservationDao, reservationTimeDao, themeDao, memberDao);

        reservationTimeDao.add(new ReservationTime(1L, LocalTime.of(10, 0)));
        themeDao.add(new Theme(1L, "레벨1", "탈출하기", "http://~"));
    }

    @Test
    @DisplayName("예약 추가를 할 수 있다.")
    void createReservation() {
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.of(2024, 4, 26), 1L, 1L);
        Member member = Member.generateNormalMember("프리", "phree@woowa.com", "password");
        Reservation actual = reservationService.createReservation(request, member);

        assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getMember().getName()).isEqualTo("프리");
            assertThat(actual.getDate()).isEqualTo(LocalDate.of(2024, 4, 26));
            assertThat(actual.getTime().getId()).isEqualTo(1L);
            assertThat(actual.getTheme().getId()).isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("createReservationAfterNow 메서드를 사용하면 이전 시간으로의 예약은 추가할 수 없다.")
    void createReservationAfterNow() {
        // Given
        Member member = Member.generateNormalMember("프리", "phree@woowa.com", "password");
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.of(2024, 4, 26), 1L, 1L);

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservationAfterNow(request, member))
                .isInstanceOf(NotCorrectDateTimeException.class)
                .hasMessage("지나간 날짜와 시간에 대한 예약 생성은 불가능하다.");
    }

    @Test
    @DisplayName("날짜와 시간, 테마가 같은 경우에는 예약 추가를 할 수 없다.")
    void cannotCreateReservation() {
        // Given
        Member member = Member.generateNormalMember("프리", "phree@woowa.com", "password");
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.of(2024, 4, 26), 1L, 1L);
        ReservationCreateRequest request2 = new ReservationCreateRequest(LocalDate.of(2024, 4, 26), 1L, 1L);
        reservationService.createReservation(request, member);

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(request2, member))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("모든 예약 정보를 가져올 수 있다.")
    void findAllReservations() {
        Member member = Member.generateNormalMember("프리", "phree@woowa.com", "password");
        ReservationCreateRequest request1 = new ReservationCreateRequest(LocalDate.of(2024, 4, 26), 1L, 1L);
        ReservationCreateRequest request2 = new ReservationCreateRequest(LocalDate.of(2024, 4, 28), 1L, 1L);

        reservationService.createReservation(request1, member);
        reservationService.createReservation(request2, member);

        assertThat(reservationService.findAllReservations()).hasSize(2);
    }

    @Test
    @DisplayName("예약을 id를 통해 제거할 수 있다.")
    void deleteReservationById() {
        Member member = Member.generateNormalMember("프리", "phree@woowa.com", "password");
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.of(2024, 4, 26), 1L, 1L);
        reservationService.createReservation(request, member);

        reservationService.deleteReservationById(1L);

        assertThat(reservationService.findAllReservations()).hasSize(0);
    }

    @Test
    void 멤버와_테마_날짜로_필터링하여_검색할_수_있다() {
        // TODO: Stub 공부 후 작성하기
        // Given
        // When
        // Then
    }
}
