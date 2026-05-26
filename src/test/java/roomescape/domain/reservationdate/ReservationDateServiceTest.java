package roomescape.domain.reservationdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationdate.admin.dto.AdminReservationDateResponse;
import roomescape.domain.reservationdate.admin.dto.CreateReservationDateRequest;
import roomescape.domain.reservationdate.admin.dto.CreateReservationDateResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.fake.FakeReservationDateRepository;
import roomescape.support.fake.FakeReservationRepository;

class ReservationDateServiceTest {

    private FakeReservationRepository reservationRepository;
    private FakeReservationDateRepository reservationDateRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationDateRepository = new FakeReservationDateRepository();
    }

    @Test
    @DisplayName("예약 날짜를 생성한다.")
    void createReservationDate() {
        // given
        ReservationDateService reservationDateService = new ReservationDateService(
            reservationRepository,
            reservationDateRepository
        );

        // when
        CreateReservationDateResponse response = reservationDateService.createReservationDate(
            new CreateReservationDateRequest(LocalDate.of(2026, 5, 4))
        );
        ReservationDate reservationDate = reservationDateRepository.findById(response.id()).orElseThrow();

        // then
        assertSoftly(softly -> {
            assertThat(response.id()).isEqualTo(reservationDate.getId());
            assertThat(response.reservationDate()).isEqualTo(LocalDate.of(2026, 5, 4));
            assertThat(reservationDate.getDate()).isEqualTo(LocalDate.of(2026, 5, 4));
        });
    }

    @Test
    @DisplayName("예약 날짜 목록을 조회한다.")
    void getReservationDateList() {
        // given
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 4)));
        ReservationDateService reservationDateService = new ReservationDateService(
            reservationRepository,
            reservationDateRepository
        );

        // when
        List<AdminReservationDateResponse> responses = reservationDateService.getAllReservationDateForAdmin();

        // then
        assertSoftly(softly -> {
            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().id()).isEqualTo(reservationDate.getId());
            assertThat(responses.getFirst().reservationDate()).isEqualTo(LocalDate.of(2026, 5, 4));
        });
    }

    @Test
    @DisplayName("이미 예약이 존재하는 날짜는 삭제할 수 없다.")
    void throwExceptionWhenDeletingDateInUse() {
        // given
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 4)));
        reservationRepository.save(
            Reservation.createWithoutId(reservationDate,
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "공포", "무서운 테마", "theme-url")
            )
        );
        ReservationDateService reservationDateService = new ReservationDateService(
            reservationRepository,
            reservationDateRepository
        );

        // when & then
        assertThatThrownBy(() -> reservationDateService.deleteReservationDate(reservationDate.getId()))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 예약이 존재하는 날짜는 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("예약이 없는 날짜는 삭제한다.")
    void deleteDateWhenNoReservationExists() {
        // given
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.of(2026, 5, 4)));
        ReservationDateService reservationDateService = new ReservationDateService(
            reservationRepository,
            reservationDateRepository
        );

        // when
        reservationDateService.deleteReservationDate(reservationDate.getId());

        // then
        assertThat(reservationDateRepository.findById(reservationDate.getId())).isEmpty();
    }
}
