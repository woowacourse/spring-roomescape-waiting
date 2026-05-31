package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.reservation.ReservationResponses;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.StoreManagementForbiddenException;
import roomescape.fixture.Fixtures;
import roomescape.repository.fake.FakeReservationRepository;
import roomescape.repository.fake.FakeReservationTimeRepository;
import roomescape.repository.fake.FakeStoreRepository;
import roomescape.repository.fake.FakeThemeRepository;
import roomescape.repository.fake.FakeUserRepository;

class AdminReservationServiceTest {
    private FakeReservationRepository reservationRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeThemeRepository themeRepository;
    private FakeUserRepository userRepository;
    private FakeStoreRepository storeRepository;
    private AdminReservationService service;
    private User manager;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository(reservationRepository);
        userRepository = new FakeUserRepository();
        storeRepository = new FakeStoreRepository();
        storeRepository.save(Fixtures.store("매장"));
        service = new AdminReservationService(reservationRepository, storeRepository);
        manager = buildUser("매니저");
        storeRepository.assignManager(Fixtures.DEFAULT_STORE_ID, manager.getId());
    }

    @Test
    void getReservations_다음_페이지가_있으면_hasNext가_true() {
        User user = buildUser("A");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 1)));
        reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 2)));
        reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 3)));

        ReservationResponses responses = service.getReservations(0, 2, null, manager.getId());

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.hasNext()).isTrue();
    }

    @Test
    void getReservations_다음_페이지가_없으면_hasNext가_false() {
        User user = buildUser("A");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 1)));
        reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 2)));

        ReservationResponses responses = service.getReservations(0, 2, null, manager.getId());

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    void getReservations_name이_주어지면_해당_이름의_예약만_반환한다() {
        User brown = buildUser("브라운");
        User other = buildUser("다른사람");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationRepository.save(buildReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 1)));
        reservationRepository.save(buildReservation(other, themeId, timeId, LocalDate.of(2026, 5, 2)));
        reservationRepository.save(buildReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 3)));

        ReservationResponses responses = service.getReservations(0, 10, "브라운", manager.getId());

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.reservations()).extracting("name").containsOnly("브라운");
    }

    @Test
    void getReservations_담당하는_매장의_예약만_반환한다() {
        User user = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long mine = reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 1)));
        reservationRepository.save(buildReservationInStore(user, themeId, timeId, LocalDate.of(2026, 5, 2), 999L));

        ReservationResponses responses = service.getReservations(0, 10, null, manager.getId());

        assertThat(responses.reservations()).extracting("id").containsExactly(mine);
    }

    @Test
    void getReservations_담당_매장이_없으면_빈_목록() {
        User stranger = buildUser("무관리자");
        User user = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 1)));

        ReservationResponses responses = service.getReservations(0, 10, null, stranger.getId());

        assertThat(responses.reservations()).isEmpty();
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    void deleteReservation_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.deleteReservation(9999L, manager.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("예약")
                .hasMessageContaining("9999");
    }

    @Test
    void deleteReservation_삭제후_조회되지_않는다() {
        User user = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long reservationId = reservationRepository.save(
                buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 6)));

        service.deleteReservation(reservationId, manager.getId());

        ReservationResponses responses = service.getReservations(0, 10, null, manager.getId());
        assertThat(responses.reservations()).extracting("id").doesNotContain(reservationId);
    }

    @Test
    void deleteReservation_담당하지_않는_매장_예약이면_StoreManagementForbiddenException() {
        User user = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long reservationId = reservationRepository.save(
                buildReservationInStore(user, themeId, timeId, LocalDate.of(2026, 5, 6), 999L));

        assertThatThrownBy(() -> service.deleteReservation(reservationId, manager.getId()))
                .isInstanceOf(StoreManagementForbiddenException.class);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    /**
     * 헬퍼메서드
     */
    private User buildUser(String name) {
        Long id = userRepository.save(Fixtures.member(name));
        return userRepository.findById(id).orElseThrow();
    }

    private Reservation buildReservation(User user, Long themeId, Long timeId, LocalDate date) {
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        ReservationTime time = reservationTimeRepository.findById(timeId).orElseThrow();
        return Fixtures.reservation(user, theme, date, time);
    }

    private Reservation buildReservationInStore(User user, Long themeId, Long timeId, LocalDate date, long storeId) {
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        ReservationTime time = reservationTimeRepository.findById(timeId).orElseThrow();
        return new Reservation(null, user, theme, date, time, Fixtures.storeWithId(storeId, "다른매장"),
                ReservationStatus.RESERVED);
    }
}
