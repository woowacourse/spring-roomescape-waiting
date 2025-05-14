package roomescape.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.dao.ReservationDao;
import roomescape.dto.request.ReservationSearchDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

public class FakeReservationDao implements ReservationDao {

    private final Map<Long, Reservation> database = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1L);

    @Override
    public Long saveReservation(Reservation reservation) {
        Long id = nextId.getAndIncrement();
        ReservationTime time = reservation.getReservationTime();
        Theme theme = reservation.getTheme();
        Member member = reservation.getMember();

        Reservation savedReservation = new Reservation(
                id,
                reservation.getDate(),
                new ReservationTime(time.getId(), time.getStartAt()),
                new Theme(theme.getId(), theme.getName(),
                        theme.getDescription(), theme.getThumbnail()),
                new Member(member.getId(), member.getName(), member.getEmail(), member.getPassword(), member.getRole())
        );
        database.put(id, savedReservation);
        return id;
    }

    @Override
    public void deleteById(Long id) {
        database.remove(id);
    }

    @Override
    public Optional<Reservation> findByDateAndTime(Reservation reservation) {
        return database.values().stream()
                .filter(reservation1 -> reservation1.getDate().equals(reservation.getDate()))
                .filter(reservation1 -> reservation1.getReservationTime().getId()
                        .equals(reservation.getReservationTime().getId()))
                .findAny();
    }

    @Override
    public List<Reservation> findAll(ReservationSearchDto reservationSearchDto) {
        return List.of();
    }
}
