package roomescape.unit.service.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.reservationtime.AddReservationTimeDto;
import roomescape.dto.reservationtime.AvailableTimeRequestDto;
import roomescape.dto.theme.AddThemeDto;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;
import roomescape.service.reservation.ReservationService;
import roomescape.unit.config.ServiceFixture;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationServiceTest {

    static ReservationService reservationService;
    static ReservationTimeRepository reservationTimeRepository;
    static ReservationRepository reservationRepository;
    static ThemeRepository themeRepository;

    @BeforeEach
    void setup() {
        reservationRepository = ServiceFixture.fakeReservationRepository();
        reservationTimeRepository = ServiceFixture.fakeReservationTimeRepository();
        themeRepository = ServiceFixture.fakeThemeRepository();

        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    void 예약을_추가하고_조회할_수_있다() {
        Long reservationTimeId = reservationTimeRepository.save(
                new AddReservationTimeDto(LocalTime.now().plusHours(1L)).toEntity());
        Long themeId = themeRepository.save(new AddThemeDto("공포의 숲", "무서운 테마", "image.png").toEntity());

        AddReservationDto request = new AddReservationDto("praisebak", LocalDate.now().plusDays(1L), reservationTimeId,
                themeId);

        reservationService.addReservation(request);

        assertThat(reservationService.allReservations().size()).isEqualTo(1);
    }

    @Test
    void 이전_날짜에_예약할_수_없다() {
        Long reservationTimeId = reservationTimeRepository.save(
                new AddReservationTimeDto(LocalTime.now().plusHours(1L)).toEntity());
        Long themeId = themeRepository.save(new AddThemeDto("과거의 방", "옛날 테마", "past.png").toEntity());

        assertThatThrownBy(() -> reservationService.addReservation(
                new AddReservationDto("투다", LocalDate.now().minusDays(1), reservationTimeId, themeId)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 같은날짜일시_이전_시간에_예약할_수_없다() {
        Long reservationTimeId = reservationTimeRepository.save(
                new AddReservationTimeDto(LocalTime.now().minusHours(1L)).toEntity());
        Long themeId = themeRepository.save(new AddThemeDto("지각의 방", "지각 금지", "late.png").toEntity());

        assertThatThrownBy(() -> reservationService.addReservation(
                new AddReservationDto("투다", LocalDate.now(), reservationTimeId, themeId)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이후_날짜에_예약할_수_있다() {
        Long reservationTimeId = reservationTimeRepository.save(
                new AddReservationTimeDto(LocalTime.now().plusHours(1L)).toEntity());
        Long themeId = themeRepository.save(new AddThemeDto("미래의 방", "SF 컨셉", "future.png").toEntity());

        assertThatCode(() -> reservationService.addReservation(
                new AddReservationDto("투다", LocalDate.now().plusDays(1), reservationTimeId, themeId)))
                .doesNotThrowAnyException();
    }

    @Test
    void 같은날짜일시_이후_시간_예약할_수_있다() {
        Long reservationTimeId = reservationTimeRepository.save(
                new AddReservationTimeDto(LocalTime.now().plusMinutes(1L)).toEntity());
        Long themeId = themeRepository.save(new AddThemeDto("정시의 방", "시간 엄수", "on_time.png").toEntity());

        assertThatCode(() -> reservationService.addReservation(
                new AddReservationDto("투다", LocalDate.now(), reservationTimeId, themeId)))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약을_삭제하고_조회할_수_있다() {
        Long reservationTimeId = reservationTimeRepository.save(
                new AddReservationTimeDto(LocalTime.now().plusMinutes(1L)).toEntity());
        Long themeId = themeRepository.save(new AddThemeDto("삭제의 방", "삭제 가능", "delete.png").toEntity());

        long id = reservationService.addReservation(
                new AddReservationDto("praisebak", LocalDate.now().plusDays(1L), reservationTimeId, themeId));
        assertThat(reservationService.allReservations().size()).isEqualTo(1);
        reservationService.deleteReservation(id);
        assertThat(reservationService.allReservations().size()).isEqualTo(0);
    }

    @Test
    void 중복_예약은_불가능하다() {
        LocalTime localTime = LocalTime.now().plusMinutes(1L);
        Long reservationTimeId = reservationTimeRepository.save(new AddReservationTimeDto(localTime).toEntity());
        Long themeId = themeRepository.save(new AddThemeDto("중복 금지 방", "한 번만 가능", "unique.png").toEntity());

        reservationService.addReservation(
                new AddReservationDto("투다", LocalDate.now(), reservationTimeId, themeId));

        assertThatThrownBy(() -> reservationService.addReservation(
                new AddReservationDto("투다", LocalDate.now(), reservationTimeId, themeId)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 선택된_테마와_날짜에_대해서_가능한_시간들을_확인할_수_있다() {
        LocalTime firstTime = LocalTime.now().plusMinutes(1L);
        LocalTime secondTime = LocalTime.now().plusMinutes(2L);

        LocalDate today = LocalDate.now();
        Long firstReservationTimeId = reservationTimeRepository.save(new AddReservationTimeDto(firstTime).toEntity());
        Long secondReservationTimeId = reservationTimeRepository.save(new AddReservationTimeDto(secondTime).toEntity());
        Long themeId = themeRepository.save(new AddThemeDto("테마", "테마2", "unique.png").toEntity());

        reservationService.addReservation(
                new AddReservationDto("투다", today, firstReservationTimeId, themeId));

        AvailableTimeRequestDto availableTimeRequestDto = new AvailableTimeRequestDto(today, themeId);
        List<ReservationSlot> reservationAvailabilities = reservationService.availableReservationTimes(
                        availableTimeRequestDto)
                .getAvailableBookTimes();

        List<ReservationSlot> reservationSlots = List.of(new ReservationSlot(1L, firstTime, true),
                new ReservationSlot(2L, secondTime, false));

        assertThat(reservationAvailabilities).containsExactlyInAnyOrderElementsOf(reservationSlots);
    }

    @Test
    void 최근_일주일을_기준으로_예약이_많은_테마_10개를_확인할_수_있다() {
        for (int i = 0; i < 10; i++) {
            Theme theme = new Theme(null, "테마" + 1, "테마", "thumbnail");
            themeRepository.save(theme);
        }
        for (int i = 0; i < 6; i++) {
            LocalTime localTime = LocalTime.of(10 + i, 0);
            reservationTimeRepository.save(new ReservationTime(null, localTime));
        }

        reservationService.addReservation(new AddReservationDto("praisebak", LocalDate.now().plusDays(1), 1L, 1L));
        reservationService.addReservation(new AddReservationDto("praisebak", LocalDate.now().plusDays(1), 2L, 1L));
        reservationService.addReservation(new AddReservationDto("praisebak", LocalDate.now().plusDays(1), 3L, 1L));

        reservationService.addReservation(new AddReservationDto("praisebak", LocalDate.now().plusDays(1), 1L, 2L));
        reservationService.addReservation(new AddReservationDto("praisebak", LocalDate.now().plusDays(1), 2L, 2L));
        reservationService.addReservation(new AddReservationDto("praisebak", LocalDate.now().plusDays(1), 1L, 3L));

        List<Theme> top10Theme = reservationService.getRankingThemes(LocalDate.now().plusDays(6), 1, 7);
        assertAll(() -> {
            assertThat(top10Theme.getFirst().getId()).isEqualTo(1L);
            assertThat(top10Theme.get(1).getId()).isEqualTo(2L);
            assertThat(top10Theme.get(2).getId()).isEqualTo(3L);
        });
    }

    @Test
    void 존재하지_않는_예약을_조회시_예외가_발생한다() {
        assertThatThrownBy(() -> reservationService.getReservationById(-1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
