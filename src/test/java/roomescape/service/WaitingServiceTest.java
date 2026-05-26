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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.WaitingDao;
import roomescape.dao.jdbc.MemberJdbcDao;
import roomescape.dao.jdbc.ThemeJdbcDao;
import roomescape.dao.jdbc.TimeJdbcDao;
import roomescape.dao.jdbc.WaitingJdbcDao;
import roomescape.domain.Waiting;
import roomescape.dto.request.WaitingRequestDto;

@JdbcTest
@Import({WaitingService.class, WaitingJdbcDao.class,
        MemberJdbcDao.class, TimeJdbcDao.class, ThemeJdbcDao.class})
@ActiveProfiles("test")
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;
    @Autowired
    private WaitingDao waitingDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long memberId;
    private Long otherMemberId;
    private Long timeId;
    private Long themeId;
    private Long storeId;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", "강남점");
        storeId = jdbcTemplate.queryForObject(
                "SELECT id FROM stores WHERE name = ?", Long.class, "강남점");

        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저1", "user1@test.com", "password", "USER"
        );
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저2", "user2@test.com", "password", "USER"
        );
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
    }

    @Nested
    class Create {

        @Test
        @DisplayName("유효한 요청으로 대기를 생성한다")
        void createsWaiting() {
            WaitingRequestDto dto = new WaitingRequestDto(memberId, date, timeId, themeId, storeId);

            Waiting saved = waitingService.create(dto);

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
            WaitingRequestDto dto = new WaitingRequestDto(memberId, date, timeId, themeId, storeId);
            waitingService.create(dto);

            assertThatThrownBy(() -> waitingService.create(dto))
                    .isInstanceOf(DataAccessException.class);
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("본인 대기를 삭제한다")
        void deletesWaiting() {
            Waiting saved = waitingService.create(
                    new WaitingRequestDto(memberId, date, timeId, themeId, storeId));

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
                    new WaitingRequestDto(memberId, date, timeId, themeId, storeId));
            Waiting w2 = waitingService.create(
                    new WaitingRequestDto(otherMemberId, date, timeId, themeId, storeId));

            List<Waiting> result = waitingService.findAll();

            assertThat(result).extracting(Waiting::getId)
                    .containsExactlyInAnyOrder(w1.getId(), w2.getId());
        }
    }

    @Nested
    class FindAllByMemberId {

        @Test
        @DisplayName("멤버 ID로 본인 대기 목록만 반환한다")
        void returnsWaitingsByMemberId() {
            Waiting mine = waitingService.create(
                    new WaitingRequestDto(memberId, date, timeId, themeId, storeId));
            waitingService.create(
                    new WaitingRequestDto(otherMemberId, date, timeId, themeId, storeId));

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
            waitingService.create(new WaitingRequestDto(memberId, date, timeId, themeId, storeId));
            waitingService.create(new WaitingRequestDto(otherMemberId, date, timeId, themeId, storeId));

            assertThat(waitingService.findAllByMemberId(memberId))
                    .singleElement()
                    .extracting(Waiting::getRank)
                    .isEqualTo(1L);
            assertThat(waitingService.findAllByMemberId(otherMemberId))
                    .singleElement()
                    .extracting(Waiting::getRank)
                    .isEqualTo(2L);
        }
    }
}
