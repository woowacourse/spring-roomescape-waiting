package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import roomescape.TestClockConfig;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.reservation.response.ReservationResponses;
import roomescape.dto.reservation.response.ReservationWithStatusResponses;
import roomescape.dto.reservation.response.WaitingReservationResponse;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateWaitingReservationException;
import roomescape.exception.PastDateTimeReservationException;
import roomescape.exception.PastReservationModificationException;
import roomescape.exception.ReservationNotFoundForWaitingException;
import roomescape.exception.ReservationOwnerMismatchException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.StoreManagementForbiddenException;
import roomescape.fixture.Fixtures;
import roomescape.repository.fake.FakeReservationRepository;
import roomescape.repository.fake.FakeReservationTimeRepository;
import roomescape.repository.fake.FakeStoreRepository;
import roomescape.repository.fake.FakeThemeRepository;
import roomescape.repository.fake.FakeUserRepository;

class ReservationServiceTest {

    private FakeReservationRepository reservationRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeThemeRepository themeRepository;
    private FakeUserRepository userRepository;
    private FakeStoreRepository storeRepository;
    private ReservationService service;
    private User manager;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository(reservationRepository);
        userRepository = new FakeUserRepository();
        storeRepository = new FakeStoreRepository();
        storeRepository.save(Fixtures.store("매장"));
        service = new ReservationService(reservationRepository, themeRepository, reservationTimeRepository,
                storeRepository, new TestClockConfig().timeProvider());
        manager = buildUser("매니저");
        storeRepository.assignManager(Fixtures.DEFAULT_STORE_ID, manager.getId());
    }

    @Test
    void createReservation_id가_채워진_도메인을_반환한다() {
        User brown = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "https://thumbnail.url"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Reservation created = service.createReservation(
                Fixtures.createCommand(brown, themeId, LocalDate.of(2026, 5, 8), timeId));

        assertThat(created.getId()).isPositive();
        assertThat(created.getUser().getName()).isEqualTo("브라운");
        assertThat(created.getDate()).isEqualTo(LocalDate.of(2026, 5, 8));
        assertThat(created.getTheme().getId()).isEqualTo(themeId);
        assertThat(created.getTime().getId()).isEqualTo(timeId);
    }

    @Test
    void createReservation_과거의_날짜_시간이면_예외() {
        User brown = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "https://thumbnail.url"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(11, 0)));

        assertThatThrownBy(() -> service.createReservation(
                Fixtures.createCommand(brown, themeId, LocalDate.of(2026, 5, 5), timeId)))
                .isInstanceOf(PastDateTimeReservationException.class)
                .hasMessage("예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다.");
    }

    @Test
    void createReservation_같은_날짜시간테마_중복이면_예외() {
        User brown = buildUser("브라운");
        User other = buildUser("다른사람");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "https://thumbnail.url"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        service.createReservation(
                Fixtures.createCommand(brown, themeId, LocalDate.of(2026, 5, 8), timeId));

        assertThatThrownBy(() -> service.createReservation(
                Fixtures.createCommand(other, themeId, LocalDate.of(2026, 5, 8), timeId)))
                .isInstanceOf(DuplicateReservationException.class)
                .hasMessage("해당 날짜·시간·테마에 이미 예약이 존재합니다. 다른 날짜·시간·테마를 선택해주세요.");
    }

    @Test
    void createReservation_존재하지_않는_themeId이면_ResourceNotFoundException() {
        User brown = buildUser("브라운");
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        assertThatThrownBy(() -> service.createReservation(
                Fixtures.createCommand(brown, 9999L, LocalDate.of(2026, 5, 8), timeId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("테마")
                .hasMessageContaining("9999");
    }

    @Test
    void createReservation_존재하지_않는_timeId이면_ResourceNotFoundException() {
        User brown = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "https://thumbnail.url"));

        assertThatThrownBy(() -> service.createReservation(
                Fixtures.createCommand(brown, themeId, LocalDate.of(2026, 5, 8), 9999L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("예약 시간")
                .hasMessageContaining("9999");
    }

    @Test
    void getReservations_다음_페이지가_있으면_hasNext가_true() {
        User user = buildUser("A");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 1)));
        reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 2)));
        reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 3)));

        ReservationResponses responses = service.getReservations(0, 2, null, manager);

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

        ReservationResponses responses = service.getReservations(0, 2, null, manager);

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

        ReservationResponses responses = service.getReservations(0, 10, "브라운", manager);

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.reservations()).extracting("name").containsOnly("브라운");
    }

    @Test
    void getMyReservations_본인_예약만_반환한다() {
        User brown = buildUser("브라운");
        User other = buildUser("다른사람");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationRepository.save(buildReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 1)));
        reservationRepository.save(buildReservation(other, themeId, timeId, LocalDate.of(2026, 5, 2)));
        reservationRepository.save(buildReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 3)));

        ReservationWithStatusResponses responses = service.getMyReservations(brown);

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.reservations()).extracting("name").containsOnly("브라운");
    }

    @Test
    void getMyReservations_예약_대기는_대기_순번을_포함해_반환한다() {
        User brown = buildUser("브라운");
        User charles = buildUser("샤를");
        User aron = buildUser("아론");

        Long themeBId = themeRepository.save(new Theme(null, "공포B", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        reservationRepository.save(buildReservation(charles, themeBId, timeId, LocalDate.of(2026, 5, 2)));
        reservationRepository.save(buildWaitingReservation(aron, themeBId, timeId, LocalDate.of(2026, 5, 2)));
        reservationRepository.save(
                buildWaitingReservation(brown, themeBId, timeId, LocalDate.of(2026, 5, 2))); //브라운 B 대기 2번

        ReservationWithStatusResponses results = service.getMyReservations(brown);

        assertThat(results.waitingReservations()).hasSize(1);
        assertThat(results).extracting(responses -> responses.waitingReservations().getFirst().name()).isEqualTo("브라운");
        assertThat(results).extracting(responses -> responses.waitingReservations().getFirst().waitingOrder())
                .isEqualTo(2);
    }

    @Test
    void getMyReservations_예약과_대기가_없으면_빈_목록을_반환한다() {
        User brown = buildUser("브라운");

        ReservationWithStatusResponses responses = service.getMyReservations(brown);

        assertThat(responses.reservations()).isEmpty();
        assertThat(responses.waitingReservations()).isEmpty();
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    void getReservation_id로_단건을_조회한다() {
        User user = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long reservationId = reservationRepository.save(
                buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 6)));

        Reservation found = service.getReservation(reservationId);

        assertThat(found.getId()).isEqualTo(reservationId);
        assertThat(found.getUser().getName()).isEqualTo("브라운");
    }

    @Test
    void getReservation_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.getReservation(9999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("예약")
                .hasMessageContaining("9999");
    }

    @Test
    void deleteReservation_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.deleteReservation(9999L, manager))
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

        service.deleteReservation(reservationId, manager);

        ReservationResponses responses = service.getReservations(0, 10, null, manager);
        assertThat(responses.reservations()).extracting("id").doesNotContain(reservationId);
    }

    @Test
    void deleteReservation_담당하지_않는_매장_예약이면_StoreManagementForbiddenException() {
        User user = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long reservationId = reservationRepository.save(
                buildReservationInStore(user, themeId, timeId, LocalDate.of(2026, 5, 6), 999L));

        assertThatThrownBy(() -> service.deleteReservation(reservationId, manager))
                .isInstanceOf(StoreManagementForbiddenException.class);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    void getReservations_담당하는_매장의_예약만_반환한다() {
        User user = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long mine = reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 1)));
        reservationRepository.save(buildReservationInStore(user, themeId, timeId, LocalDate.of(2026, 5, 2), 999L));

        ReservationResponses responses = service.getReservations(0, 10, null, manager);

        assertThat(responses.reservations()).extracting("id").containsExactly(mine);
    }

    @Test
    void getReservations_담당_매장이_없으면_빈_목록() {
        User stranger = buildUser("무관리자");
        User user = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationRepository.save(buildReservation(user, themeId, timeId, LocalDate.of(2026, 5, 1)));

        ReservationResponses responses = service.getReservations(0, 10, null, stranger);

        assertThat(responses.reservations()).isEmpty();
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    void cancelOwnReservation_userId_불일치면_예외() {
        User brown = buildUser("브라운");
        User other = buildUser("다른사람");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long reservationId = reservationRepository.save(
                buildReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 8)));

        assertThatThrownBy(() -> service.cancelOwnReservation(Fixtures.cancelCommand(reservationId, other)))
                .isInstanceOf(ReservationOwnerMismatchException.class);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    void cancelOwnReservation_없는_id이면_ResourceNotFoundException() {
        User brown = buildUser("브라운");

        assertThatThrownBy(() -> service.cancelOwnReservation(Fixtures.cancelCommand(9999L, brown)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelOwnReservation_과거_예약이면_예외() {
        User brown = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long reservationId = reservationRepository.save(
                buildReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 1)));

        assertThatThrownBy(() -> service.cancelOwnReservation(Fixtures.cancelCommand(reservationId, brown)))
                .isInstanceOf(PastReservationModificationException.class);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    void updateOwnReservation_변경된_도메인을_반환한다() {
        User brown = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long themeId2 = themeRepository.save(new Theme(null, "추리", "재밌음", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long timeId2 = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(11, 0)));
        Long reservationId = reservationRepository.save(
                buildReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1)));

        Reservation updated = service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId2, LocalDate.of(2026, 6, 2), timeId2));

        assertThat(updated.getDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(updated.getTheme().getId()).isEqualTo(themeId2);
        assertThat(updated.getTime().getId()).isEqualTo(timeId2);
    }

    @Test
    void updateOwnReservation_userId_불일치면_예외() {
        User brown = buildUser("브라운");
        User other = buildUser("다른사람");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long reservationId = reservationRepository.save(
                buildReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1)));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, other, themeId, LocalDate.of(2026, 6, 2), timeId)))
                .isInstanceOf(ReservationOwnerMismatchException.class);
    }

    @Test
    void updateOwnReservation_기존_예약이_과거이면_예외() {
        User brown = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long reservationId = reservationRepository.save(
                buildReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 1)));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, LocalDate.of(2026, 6, 2), timeId)))
                .isInstanceOf(PastReservationModificationException.class);
    }

    @Test
    void updateOwnReservation_새_일정이_과거이면_예외() {
        User brown = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long reservationId = reservationRepository.save(
                buildReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1)));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, LocalDate.of(2026, 5, 1), timeId)))
                .isInstanceOf(PastDateTimeReservationException.class);
    }

    @Test
    void updateOwnReservation_새_일정이_다른_예약과_충돌하면_예외() {
        User brown = buildUser("브라운");
        User other = buildUser("다른사람");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long timeId2 = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(11, 0)));
        Long reservationId = reservationRepository.save(
                buildReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1)));
        reservationRepository.save(buildReservation(other, themeId, timeId2, LocalDate.of(2026, 6, 2)));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, LocalDate.of(2026, 6, 2), timeId2)))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    void updateOwnReservation_기존_슬롯과_동일하면_예외없이_통과() {
        User brown = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long reservationId = reservationRepository.save(
                buildReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1)));

        Reservation updated = service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, LocalDate.of(2026, 6, 1), timeId));

        assertThat(updated.getId()).isEqualTo(reservationId);
    }

    @Test
    void updateOwnReservation_없는_id이면_ResourceNotFoundException() {
        User brown = buildUser("브라운");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(9999L, brown, themeId, LocalDate.of(2026, 6, 2), timeId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /*
     * ReservationNotFoundForWaitingException
     * PastDateTimeReservationException
     * DuplicateWaitingReservationException
     */
    @Test
    void createWaitingReservation_예약_대기를_생성한다() {
        User brown = buildUser("브라운");
        User charles = buildUser("샤를");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationRepository.save(buildReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1)));

        WaitingReservationResponse result = service.createWaitingReservation(
                Fixtures.createCommand(charles, themeId, LocalDate.of(2026, 6, 1), timeId));

        assertThat(result.date()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(result.themeName()).isEqualTo("공포");
        assertThat(result.name()).isEqualTo("샤를");
        assertThat(result.time()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.waitingOrder()).isEqualTo(1);
    }

    @Test
    void createWaitingReservation_해당_슬롯에_아직_예약_확정이_없는_경우_ReservationNotFoundForWaitingException를_반환한다() {
        User charles = buildUser("샤를");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        assertThatThrownBy(() -> service.createWaitingReservation(
                Fixtures.createCommand(charles, themeId, LocalDate.of(2026, 6, 1), timeId)))
                .isInstanceOf(ReservationNotFoundForWaitingException.class);
    }

    @Test
    void createWaitingReservation_과거_날짜로_예약_대기를_거는_경우_PastDateTimeReservationException를_반환한다() {
        User brown = buildUser("브라운");
        User charles = buildUser("샤를");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationRepository.save(buildReservation(brown, themeId, timeId, LocalDate.of(1, 5, 1)));

        assertThatThrownBy(() -> service.createWaitingReservation(
                Fixtures.createCommand(charles, themeId, LocalDate.of(1, 5, 1), timeId)))
                .isInstanceOf(PastDateTimeReservationException.class);
    }

    @Test
    void createWaitingReservation_존재하지_않는_themeId이면_ResourceNotFoundException() {
        User charles = buildUser("샤를");
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        assertThatThrownBy(() -> service.createWaitingReservation(
                Fixtures.createCommand(charles, 9999L, LocalDate.of(2026, 6, 1), timeId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("테마")
                .hasMessageContaining("9999");
    }

    @Test
    void createWaitingReservation_이미_본인_예약_대기가_존재하면_예외() {
        User brown = buildUser("브라운");
        User charles = buildUser("샤를");
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        reservationRepository.save(buildReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1)));
        reservationRepository.save(buildWaitingReservation(charles, themeId, timeId, LocalDate.of(2026, 6, 1)));

        assertThatThrownBy(() -> service.createWaitingReservation(
                Fixtures.createCommand(charles, themeId, LocalDate.of(2026, 6, 1), timeId)))
                .isInstanceOf(DuplicateWaitingReservationException.class);
    }

    private User buildUser(String name) {
        Long id = userRepository.save(Fixtures.member(name));
        return userRepository.findById(id).orElseThrow();
    }

    private Reservation buildReservation(User user, Long themeId, Long timeId, LocalDate date) {
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        ReservationTime time = reservationTimeRepository.findById(timeId).orElseThrow();
        return Fixtures.reservation(user, theme, date, time);
    }

    private Reservation buildWaitingReservation(User user, Long themeId, Long timeId, LocalDate date) {
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        ReservationTime time = reservationTimeRepository.findById(timeId).orElseThrow();
        return Fixtures.reservation(user, theme, date, time, ReservationStatus.WAITING);
    }

    private Reservation buildReservationInStore(User user, Long themeId, Long timeId, LocalDate date, long storeId) {
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        ReservationTime time = reservationTimeRepository.findById(timeId).orElseThrow();
        return new Reservation(null, user, theme, date, time, Fixtures.storeWithId(storeId, "다른매장"),
                ReservationStatus.RESERVED);
    }
}
