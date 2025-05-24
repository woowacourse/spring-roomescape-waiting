package roomescape.domain.timeslot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static roomescape.TestFixtures.anyTimeSlot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.exception.NotFoundException;

@DataJpaTest
class TimeSlotJpaRepositoryTest {

    @Autowired
    private TimeSlotRepository timeSlotRepository;
    @Autowired
    private TestEntityManager entityManager;

    private TimeSlot savedTimeSlot;

    @BeforeEach
    void setUp() {
        savedTimeSlot = timeSlotRepository.save(anyTimeSlot());
        entityManager.clear();
    }

    @Test
    @DisplayName("아이디에 해당하는 타임 슬롯을 삭제한다.")
    void deleteByIdWhenFound() {
        var id = savedTimeSlot.id();

        assertAll(
            () -> assertDoesNotThrow(() -> timeSlotRepository.deleteByIdOrElseThrow(id)),
            () -> assertThat(timeSlotRepository.findById(id)).isEmpty()
        );
    }

    @Test
    @DisplayName("타임 슬롯 삭제 시 해당 아이디의 타임 슬롯이 없으면 예외가 발생한다.")
    void deleteByIdWhenNotFound() {
        var id = savedTimeSlot.id();

        assertAll(
            () -> assertThrows(NotFoundException.class, () -> timeSlotRepository.deleteByIdOrElseThrow(1234L)),
            () -> assertThat(timeSlotRepository.findById(id)).hasValue(savedTimeSlot)
        );
    }

    @Test
    @DisplayName("아이디에 해당하는 타임 슬롯을 조회한다.")
    void getById() {
        var id = savedTimeSlot.id();

        var found = timeSlotRepository.getById(id);

        assertThat(found).isEqualTo(savedTimeSlot);
    }

    @Test
    @DisplayName("타임 슬롯 조회 시 해당 아이디의 타임 슬롯이 없으면 예외가 발생한다.")
    void getByIdWhenNotFound() {
        assertThatThrownBy(() -> timeSlotRepository.getById(1234L))
            .isInstanceOf(NotFoundException.class);
    }
}
