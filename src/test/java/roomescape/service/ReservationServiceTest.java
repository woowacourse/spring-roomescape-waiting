package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.exception.HiddenResourceException;
import roomescape.member.MemberDao;
import roomescape.reservation.ReservationDao;
import roomescape.theme.ThemeDao;
import roomescape.time.TimeDao;
import roomescape.member.dao.MemberJdbcDao;
import roomescape.order.dao.OrderJdbcDao;
import roomescape.promotion.dao.PromotionOutboxJdbcDao;
import roomescape.reservation.dao.ReservationJdbcDao;
import roomescape.store.dao.StoreJdbcDao;
import roomescape.theme.dao.ThemeJdbcDao;
import roomescape.time.dao.TimeJdbcDao;
import roomescape.waiting.dao.WaitingJdbcDao;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.order.OrderService;
import roomescape.promotion.PromotionService;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationCreator;
import roomescape.reservation.ReservationService;
import roomescape.reservation.ReservationStatus;
import roomescape.store.Store;
import roomescape.theme.Theme;
import roomescape.time.Time;
import roomescape.common.vo.Name;
import roomescape.waiting.WaitingService;
import roomescape.reservation.web.ReservationPatchDto;
import roomescape.reservation.web.ReservationRequestDto;

@JdbcTest
@Import({ReservationService.class, ReservationCreator.class, ReservationAuthorizationService.class, WaitingService.class,
        ReservationJdbcDao.class, TimeJdbcDao.class, ThemeJdbcDao.class, MemberJdbcDao.class, StoreJdbcDao.class,
        WaitingJdbcDao.class, PromotionOutboxJdbcDao.class, PromotionService.class,
        OrderJdbcDao.class, OrderService.class})
@ActiveProfiles("test")
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private TimeDao timeDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private Member otherMember;
    private Time savedTime1;
    private Time savedTime2;
    private Theme savedTheme1;
    private Theme savedTheme2;
    private Long storeId;
    private Store store;
    private ReservationRequestDto requestDto1;
    private ReservationRequestDto requestDto2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
        storeId = jdbcTemplate.queryForObject(
                "SELECT id FROM stores WHERE name = ?", Long.class, "강남점");
        store = new Store(storeId, "강남점");
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저", "user@test.com", "password", "USER"
        );
        member = memberDao.findByEmail("user@test.com").orElseThrow();
        otherMember = new Member(-1L, "타인", "other@test.com", "password", MemberRole.USER);
        savedTime1 = timeDao.insert(new Time(LocalTime.of(13, 0)));
        savedTime2 = timeDao.insert(new Time(LocalTime.of(14, 0)));
        savedTheme1 = themeDao.insert(new Theme(new Name("방탈출 이름1"), "http://thumbnail_url", "방탈출을 할 수 있다."));
        savedTheme2 = themeDao.insert(new Theme(new Name("방탈출 이름2"), "http://thumbnail_url", "방탈출을 할 수 있다."));
        requestDto1 = new ReservationRequestDto(LocalDate.now().plusDays(1), savedTime1.getId(), savedTheme1.getId(), storeId);
        requestDto2 = new ReservationRequestDto(LocalDate.now().plusDays(2), savedTime2.getId(), savedTheme2.getId(), storeId);
    }

    @Nested
    class Create {

        @Test
        @DisplayName("유효한 요청으로 예약을 생성한다")
        void createsReservation() {
            Reservation saved = reservationService.create(member, requestDto1).reservation();

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getStatus()).isEqualTo(ReservationStatus.PENDING);
            assertThat(saved.getMember()).isEqualTo(member);
            assertThat(saved.getDate()).isEqualTo(requestDto1.date());
        }

        @Test
        @DisplayName("시간이 존재하지 않으면 예외를 반환한다")
        void throwsWhenTimeNotFound() {
            ReservationRequestDto dto = new ReservationRequestDto(LocalDate.now().plusDays(1), -1L, savedTheme1.getId(), null);

            assertThatThrownBy(() -> reservationService.create(member, dto))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("테마가 존재하지 않으면 예외를 반환한다")
        void throwsWhenThemeNotFound() {
            ReservationRequestDto dto = new ReservationRequestDto(LocalDate.now().plusDays(1), savedTime1.getId(), -1L, null);

            assertThatThrownBy(() -> reservationService.create(member, dto))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("동일한 테마, 날짜, 시간으로 예약을 생성하면 예외를 반환한다")
        void throwsWhenDuplicateReservation() {
            reservationService.create(member, requestDto1);

            assertThatThrownBy(() -> reservationService.create(member, requestDto1))
                    .isInstanceOf(DuplicateEntityException.class);
        }

        @Test
        @DisplayName("과거 날짜로 예약을 생성하면 예외를 반환한다")
        void throwsWhenPastDate() {
            ReservationRequestDto pastDto = new ReservationRequestDto(
                    LocalDate.now().minusDays(1), savedTime1.getId(), savedTheme1.getId(), storeId);

            assertThatThrownBy(() -> reservationService.create(member, pastDto))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    class FindActiveById {

        @Test
        @DisplayName("BOOKED 예약을 조회한다")
        void returnsActiveReservation() {
            Reservation saved = reservationDao.insert(
                    Reservation.createByAdmin(member, requestDto1.date(), savedTime1, savedTheme1, store));

            assertThat(reservationService.findActiveById(saved.getId())).isEqualTo(saved);
        }

        @Test
        @DisplayName("CANCELED 예약을 조회하면 예외를 반환한다")
        void throwsWhenCanceled() {
            Reservation saved = reservationDao.insert(
                    Reservation.createByAdmin(member, LocalDate.now().plusDays(1), savedTime1, savedTheme1, store));
            reservationService.cancel(saved.getId(), member);

            assertThatThrownBy(() -> reservationService.findActiveById(saved.getId()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class FindAllByMemberId {

        @Test
        @DisplayName("멤버 ID로 활성 예약 목록을 조회한다")
        void returnsActiveReservationsByMemberId() {
            Reservation saved = reservationService.create(member, requestDto1).reservation();

            List<Reservation> result = reservationService.findAllByMemberId(member.getId());

            assertThat(result).isEqualTo(List.of(saved));
        }

        @Test
        @DisplayName("취소된 예약도 반환한다")
        void includesCanceledReservations() {
            Reservation saved = reservationDao.insert(
                    Reservation.createByAdmin(member, LocalDate.now().plusDays(1), savedTime1, savedTheme1, store));
            reservationService.cancel(saved.getId(), member);

            assertThat(reservationService.findAllByMemberId(member.getId())).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 멤버 ID이면 빈 목록을 반환한다")
        void returnsEmptyWhenMemberNotFound() {
            assertThat(reservationService.findAllByMemberId(-1L)).isEmpty();
        }
    }

    @Nested
    class UpdateByUser {

        @Test
        @DisplayName("본인 예약을 수정한다")
        void updatesReservation() {
            Reservation saved = reservationDao.insert(
                    Reservation.createByAdmin(member, requestDto1.date(), savedTime1, savedTheme1, store));
            LocalDate newDate = LocalDate.now().plusDays(3);
            ReservationPatchDto updateDto = new ReservationPatchDto(newDate, savedTime2.getId());

            Reservation updated = reservationService.updateByUser(saved.getId(), member, updateDto);

            assertThat(updated.getDate()).isEqualTo(newDate);
            assertThat(updated.getTime()).isEqualTo(savedTime2);
        }

        @Test
        @DisplayName("다른 사람의 예약을 수정하면 예외를 반환한다")
        void throwsWhenMemberMismatch() {
            Reservation saved = reservationService.create(member, requestDto1).reservation();
            ReservationPatchDto updateDto = new ReservationPatchDto(LocalDate.now().plusDays(3), savedTime2.getId());

            assertThatThrownBy(() -> reservationService.updateByUser(saved.getId(), otherMember, updateDto))
                    .isInstanceOf(HiddenResourceException.class);
        }

        @Test
        @DisplayName("다른 사람의 예약을 존재하지 않는 시간으로 수정해도 숨김 예외를 반환한다")
        void throwsHiddenResourceWhenMemberMismatchWithUnknownTime() {
            Reservation saved = reservationService.create(member, requestDto1).reservation();
            ReservationPatchDto updateDto = new ReservationPatchDto(LocalDate.now().plusDays(3), -1L);

            assertThatThrownBy(() -> reservationService.updateByUser(saved.getId(), otherMember, updateDto))
                    .isInstanceOf(HiddenResourceException.class);
        }

        @Test
        @DisplayName("존재하지 않는 id를 수정하면 예외를 반환한다")
        void throwsWhenIdNotFound() {
            ReservationPatchDto updateDto = new ReservationPatchDto(LocalDate.now().plusDays(3), savedTime1.getId());

            assertThatThrownBy(() -> reservationService.updateByUser(-1L, member, updateDto))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class Cancel {

        @Test
        @DisplayName("미래 예약을 취소하면 상태가 CANCELED로 변경된다")
        void cancelsReservation() {
            Reservation saved = reservationDao.insert(
                    Reservation.createByAdmin(member, LocalDate.now().plusDays(1), savedTime1, savedTheme1, store));

            reservationService.cancel(saved.getId(), member);

            Reservation canceled = reservationDao.findById(saved.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("존재하지 않는 id를 취소하면 예외를 반환한다")
        void throwsWhenIdNotFound() {
            assertThatThrownBy(() -> reservationService.cancel(-1L, member))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("이미 지난 예약을 취소하면 예외를 반환한다")
        void throwsWhenPastReservation() {
            Reservation saved = reservationDao.insert(
                    Reservation.createByAdmin(member, LocalDate.now().minusDays(1), savedTime1, savedTheme1, store));

            assertThatThrownBy(() -> reservationService.cancel(saved.getId(), member))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test
        @DisplayName("다른 사람의 예약을 취소하면 예외를 반환한다")
        void throwsWhenNotOwner() {
            Reservation saved = reservationDao.insert(
                    Reservation.createByAdmin(member, LocalDate.now().plusDays(1), savedTime1, savedTheme1, store));

            assertThatThrownBy(() -> reservationService.cancel(saved.getId(), otherMember))
                    .isInstanceOf(HiddenResourceException.class);
        }
    }
}
