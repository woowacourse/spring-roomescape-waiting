package roomescape.service.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Role;
import roomescape.repository.ReservationRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationDeleteServiceTest {
    private final ReservationDeleteService service;
    private final ReservationRepository repository;

    @Autowired
    public ReservationDeleteServiceTest(final ReservationDeleteService service, final ReservationRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    private long getReservationSize() {
        return repository.findAll().size();
    }

    @DisplayName("존재하는 예약을 삭제하면 Db에도 삭제된다.")
    @Test
    void given_initialSize_when_delete_then_deletedItemInDb() {
        //given
        long initialSize = getReservationSize();
        //when
        service.delete(7L);
        long afterCreateSize = getReservationSize();
        //then
        assertThat(afterCreateSize).isEqualTo(initialSize - 1);
    }


    @DisplayName("예약이 제거되면 우선 대기중인 상태의 예약이 예약 상태가 된다.")
    @Test
    void given_when_deleteReservationHasWaitingReservation_then_stateChangedToReserved() {
        //when
        service.delete(8L);
        //then
        assertThat(repository.findById(9L).get().getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @DisplayName("소유자가 아닌 회원의 대기중인 예약을 제거할 수 없다.")
    @Test
    void given_differentMemberId_when_deleteByIdAndOwner_then_notDeleted() {
        //given
        long initialSize = getReservationSize();
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(1L, "user@test.com", password, "duck", Role.USER);
        //when
        service.deleteByIdAndOwner(9L, member);
        long afterCreateSize = getReservationSize();
        //then
        assertThat(afterCreateSize).isEqualTo(initialSize);
    }

    @DisplayName("대기중인 예약을 제거할 수 있다.")
    @Test
    void given_when_deleteWaitingById_then_deleted() {
        //given
        long initialSize = getReservationSize();
        //when
        service.deleteWaitingById(9L);
        long afterCreateSize = getReservationSize();
        //then
        assertThat(afterCreateSize).isEqualTo(initialSize - 1);
    }
}
