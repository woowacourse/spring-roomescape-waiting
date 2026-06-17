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
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.waiting.WaitingDao;
import roomescape.reservation.dao.ReservationJdbcDao;
import roomescape.waiting.dao.WaitingJdbcDao;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingService;
import roomescape.waiting.web.WaitingRequestDto;

@JdbcTest
@Import({WaitingService.class, WaitingJdbcDao.class, ReservationJdbcDao.class})
@ActiveProfiles("test")
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;
    @Autowired
    private WaitingDao waitingDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long reserverId;
    private Long memberId;
    private Long otherMemberId;
    private Long timeId;
    private Long themeId;
    private Long storeId;
    private Long otherStoreId;
    private LocalDate date;
    private Member member;
    private Member otherMember;
    private Member reserver;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "홍대점");
        storeId = jdbcTemplate.queryForObject(
                "SELECT id FROM stores WHERE name = ?", Long.class, "강남점");
        otherStoreId = jdbcTemplate.queryForObject(
                "SELECT id FROM stores WHERE name = ?", Long.class, "홍대점");

        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "예약자", "reserver@test.com", "password", "USER"
        );
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저1", "user1@test.com", "password", "USER"
        );
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저2", "user2@test.com", "password", "USER"
        );
        reserverId = jdbcTemplate.queryForObject(
                "SELECT id FROM members WHERE email = ?", Long.class, "reserver@test.com");
        memberId = jdbcTemplate.queryForObject(
                "SELECT id FROM members WHERE email = ?", Long.class, "user1@test.com");
        otherMemberId = jdbcTemplate.queryForObject(
                "SELECT id FROM members WHERE email = ?", Long.class, "user2@test.com");

        jdbcTemplate.update("INSERT INTO times(start_at) VALUES (?)", LocalTime.of(13, 0));
        timeId = jdbcTemplate.queryForObject(
                "SELECT id FROM times WHERE start_at = ?", Long.class, LocalTime.of(13, 0));

        jdbcTemplate.update(
                "INSERT INTO themes(name, thumbnail_url, description) VALUES (?, ?, ?)",
                "방탈출1", "http://thumbnail_url", "설명"
        );
        themeId = jdbcTemplate.queryForObject(
                "SELECT id FROM themes WHERE name = ?", Long.class, "방탈출1");

        date = LocalDate.now().plusDays(1);

        reserver = new Member(reserverId, "예약자", "reserver@test.com", "password", MemberRole.USER);
        member = new Member(memberId, "유저1", "user1@test.com", "password", MemberRole.USER);
        otherMember = new Member(otherMemberId, "유저2", "user2@test.com", "password", MemberRole.USER);

        seedReservation(reserverId, storeId);
        seedReservation(reserverId, otherStoreId);
    }

    private void seedReservation(Long memberId, Long storeId) {
        jdbcTemplate.update(
                "INSERT INTO reservations(member_id, date, time_id, theme_id, store_id, status) "
                        + "VALUES (?, ?, ?, ?, ?, 'BOOKED')",
                memberId, date, timeId, themeId, storeId);
    }

    private Member saveMember(String name, String email) {
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                name, email, "password", "USER"
        );
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM members WHERE email = ?", Long.class, email);
        return new Member(id, name, email, "password", MemberRole.USER);
    }

    private Long rankOf(List<Waiting> waitings, Long waitingId) {
        return waitings.stream()
                .filter(waiting -> waiting.getId().equals(waitingId))
                .findFirst()
                .orElseThrow()
                .getRank();
    }

    @Nested
    class Create {

        @Test
        @DisplayName("유효한 요청으로 대기를 생성한다")
        void createsWaiting() {
            WaitingRequestDto dto = new WaitingRequestDto(date, timeId, themeId, storeId);

            Waiting saved = waitingService.create(dto, member);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getMember().getId()).isEqualTo(memberId);
            assertThat(saved.getDate()).isEqualTo(date);
            assertThat(saved.getTime().getId()).isEqualTo(timeId);
            assertThat(saved.getTheme().getId()).isEqualTo(themeId);
            assertThat(saved.getStoreId()).isEqualTo(storeId);
        }

        @Test
        @DisplayName("같은 사용자가 같은 슬롯에 중복 대기하면 예외를 반환한다")
        void throwsWhenDuplicate() {
            WaitingRequestDto dto = new WaitingRequestDto(date, timeId, themeId, storeId);
            waitingService.create(dto, member);

            assertThatThrownBy(() -> waitingService.create(dto, member))
                    .isInstanceOf(DuplicateEntityException.class);
        }

        @Test
        @DisplayName("해당 슬롯에 예약이 없으면 대기 생성 시 예외를 반환한다")
        void throwsWhenNoReservation() {
            LocalDate emptyDate = date.plusDays(7);
            WaitingRequestDto dto = new WaitingRequestDto(emptyDate, timeId, themeId, storeId);

            assertThatThrownBy(() -> waitingService.create(dto, member))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test
        @DisplayName("자신의 예약 슬롯에 대기를 신청하면 예외를 반환한다")
        void throwsWhenOwnReservation() {
            WaitingRequestDto dto = new WaitingRequestDto(date, timeId, themeId, storeId);

            assertThatThrownBy(() -> waitingService.create(dto, reserver))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test
        @DisplayName("같은 슬롯의 대기가 5개이면 더 이상 대기할 수 없다")
        void throwsWhenWaitingCountLimitExceeded() {
            WaitingRequestDto dto = new WaitingRequestDto(date, timeId, themeId, storeId);
            for (int i = 0; i < 5; i++) {
                Member waitingMember = saveMember("대기" + i, "waiting" + i + "@test.com");
                waitingService.create(dto, waitingMember);
            }
            Member sixthMember = saveMember("대기6", "waiting6@test.com");

            assertThatThrownBy(() -> waitingService.create(dto, sixthMember))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("본인 대기를 삭제한다")
        void deletesWaiting() {
            Waiting saved = waitingService.create(
                    new WaitingRequestDto(date, timeId, themeId, storeId), member);

            waitingService.delete(saved.getId());

            assertThat(waitingDao.existsById(saved.getId())).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 id를 삭제하면 예외를 반환한다")
        void throwsWhenIdNotFound() {
            assertThatThrownBy(() -> waitingService.delete(-1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class FindAll {

        @Test
        @DisplayName("대기가 없으면 빈 목록을 반환한다")
        void returnsEmptyList() {
            assertThat(waitingService.findAll()).isEmpty();
        }

        @Test
        @DisplayName("전체 대기 목록을 반환한다")
        void returnsAllWaitings() {
            Waiting w1 = waitingService.create(
                    new WaitingRequestDto(date, timeId, themeId, storeId), member);
            Waiting w2 = waitingService.create(
                    new WaitingRequestDto(date, timeId, themeId, storeId), otherMember);

            List<Waiting> result = waitingService.findAll();

            assertThat(result).extracting(Waiting::getId)
                    .containsExactlyInAnyOrder(w1.getId(), w2.getId());
        }

        @Test
        @DisplayName("전체 대기 조회 시 같은 슬롯 안에서만 순번을 부여한다")
        void assignsRankPerSlotWhenFindAll() {
            Waiting first = waitingService.create(
                    new WaitingRequestDto(date, timeId, themeId, storeId), member);
            Waiting second = waitingService.create(
                    new WaitingRequestDto(date, timeId, themeId, storeId), otherMember);
            Member thirdMember = saveMember("유저3", "user3@test.com");
            Waiting otherSlot = waitingService.create(
                    new WaitingRequestDto(date, timeId, themeId, otherStoreId), thirdMember);

            List<Waiting> result = waitingService.findAll();

            assertThat(rankOf(result, first.getId())).isEqualTo(1L);
            assertThat(rankOf(result, second.getId())).isEqualTo(2L);
            assertThat(rankOf(result, otherSlot.getId())).isEqualTo(1L);
        }
    }

    @Nested
    class FindAllByMemberId {

        @Test
        @DisplayName("멤버 ID로 본인 대기 목록만 반환한다")
        void returnsWaitingsByMemberId() {
            Waiting mine = waitingService.create(
                    new WaitingRequestDto(date, timeId, themeId, storeId), member);
            waitingService.create(
                    new WaitingRequestDto(date, timeId, themeId, storeId), otherMember);

            List<Waiting> result = waitingService.findAllByMemberId(memberId);

            assertThat(result).extracting(Waiting::getId).containsExactly(mine.getId());
        }

        @Test
        @DisplayName("존재하지 않는 멤버 ID이면 빈 목록을 반환한다")
        void returnsEmptyWhenMemberNotFound() {
            assertThat(waitingService.findAllByMemberId(-1L)).isEmpty();
        }

        @Test
        @DisplayName("같은 슬롯의 대기는 신청 순서대로 순번이 부여된다")
        void assignsOrderInSequence() {
            waitingService.create(new WaitingRequestDto(date, timeId, themeId, storeId), member);
            waitingService.create(new WaitingRequestDto(date, timeId, themeId, storeId), otherMember);

            assertThat(waitingService.findAllByMemberId(memberId))
                    .singleElement()
                    .extracting(Waiting::getRank)
                    .isEqualTo(1L);
            assertThat(waitingService.findAllByMemberId(otherMemberId))
                    .singleElement()
                    .extracting(Waiting::getRank)
                    .isEqualTo(2L);
        }

        @Test
        @DisplayName("다른 매장의 대기는 순번에 포함되지 않는다")
        void rankIsolatedPerStore() {
            waitingService.create(new WaitingRequestDto(date, timeId, themeId, storeId), member);
            waitingService.create(new WaitingRequestDto(date, timeId, themeId, otherStoreId), otherMember);

            assertThat(waitingService.findAllByMemberId(memberId))
                    .singleElement()
                    .extracting(Waiting::getRank)
                    .isEqualTo(1L);
            assertThat(waitingService.findAllByMemberId(otherMemberId))
                    .singleElement()
                    .extracting(Waiting::getRank)
                    .isEqualTo(1L);
        }
    }

    @Nested
    class FindAllByStoreId {

        @Test
        @DisplayName("매장 ID로 해당 매장의 대기만 반환한다")
        void returnsWaitingsByStoreId() {
            Waiting mine = waitingService.create(
                    new WaitingRequestDto(date, timeId, themeId, storeId), member);
            waitingService.create(
                    new WaitingRequestDto(date, timeId, themeId, otherStoreId), otherMember);

            List<Waiting> result = waitingService.findAllByStoreId(storeId);

            assertThat(result).extracting(Waiting::getId).containsExactly(mine.getId());
        }

        @Test
        @DisplayName("해당 매장의 대기가 없으면 빈 목록을 반환한다")
        void returnsEmptyWhenStoreHasNoWaitings() {
            waitingService.create(new WaitingRequestDto(date, timeId, themeId, storeId), member);

            assertThat(waitingService.findAllByStoreId(otherStoreId)).isEmpty();
        }
    }
}
