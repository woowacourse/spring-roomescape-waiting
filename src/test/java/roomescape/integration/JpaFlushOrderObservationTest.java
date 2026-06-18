package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@DataJpaTest
class JpaFlushOrderObservationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    /*
     * 1. 시도한 코드
     *    기존 예약을 삭제한 뒤 flush하지 않고 같은 슬롯에 새로운 예약을 저장한다.
     *
     * 2. 예측 SQL
     *    기존 예약의 DELETE가 실행된 뒤 새로운 예약의 INSERT가 실행될 것으로 예측했다.
     *
     * 3. 실제 SQL
     *    기존 예약의 DELETE는 실행되지 않고 새로운 예약의 INSERT가 먼저 실행되어
     *    슬롯 유일성 제약 조건 위반이 발생한다.
     *
     * 4. 왜 그런가
     *    deleteById는 엔티티를 삭제 상태로 만들지만 즉시 DELETE SQL을 실행하지 않는다.
     *    반면 Reservation은 IDENTITY 전략을 사용하므로 새 예약을 저장할 때 INSERT가 즉시 실행되고,
     *    DB에는 기존 예약이 남아 있어 같은 슬롯에 대한 유일성 제약 조건을 위반한다.
     */
    @Test
    void 기존_예약_삭제를_flush하지_않고_같은_슬롯에_새_예약을_저장하면_유일성_제약을_위반한다() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme(
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        ));
        Slot slot = new Slot(LocalDate.of(2026, 8, 5), time, theme);
        Reservation existing = reservationRepository.saveAndFlush(new Reservation(new Member("민욱"), slot));

        reservationRepository.deleteById(existing.getId()); // without flush

        assertThatThrownBy(() -> reservationRepository.save(new Reservation(new Member("브라운"), slot)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /*
     * 1. 시도한 코드
     *    기존 예약을 삭제하고 명시적으로 flush한 뒤 같은 슬롯에 새로운 예약을 저장한다.
     *
     * 2. 예측 SQL
     *    기존 예약의 DELETE가 먼저 실행되고 새로운 예약의 INSERT가 실행될 것으로 예측했다.
     *
     * 3. 실제 SQL
     *    flush 시점에 기존 예약의 DELETE가 실행된 뒤 새로운 예약의 INSERT가 실행되어
     *    슬롯 유일성 제약 조건을 위반하지 않는다.
     *
     * 4. 왜 그런가
     *    flush가 영속성 컨텍스트의 삭제 변경 사항을 DB에 먼저 반영해 기존 슬롯을 비운다.
     *    따라서 이후 같은 슬롯에 새로운 예약을 INSERT해도 기존 예약과 충돌하지 않는다.
     */
    @Test
    void 기존_예약_삭제를_flush하고_같은_슬롯에_새_예약을_저장하면_유일성_제약을_위반하지_않는다() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme(
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        ));
        Slot slot = new Slot(LocalDate.of(2026, 8, 5), time, theme);
        Reservation existing = reservationRepository.saveAndFlush(new Reservation(new Member("민욱"), slot));

        reservationRepository.deleteById(existing.getId()); // with flush
        reservationRepository.flush();

        assertThatCode(() -> reservationRepository.save(new Reservation(new Member("브라운"), slot)))
                .doesNotThrowAnyException();
    }
}
