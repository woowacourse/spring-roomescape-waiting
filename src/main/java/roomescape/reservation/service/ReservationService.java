package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.common.exception.PastDateException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.AvailableReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    
    public Reservation save(final Member member, final LocalDate date, final Long timeId, final Long themeId) {
        if (!date.isAfter(LocalDate.now())) {
            throw new PastDateException("과거 시간은 예약 등록을 할 수 없습니다. date = " + date);
        }
        final ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 시간 데이터가 존재하지 않습니다. id = " + timeId));
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + themeId));
        if (reservationRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme)) {
            throw new DataExistException("해당 시간에 이미 예약된 테마입니다.");
        }
        final Reservation reservation = new Reservation(member, date, reservationTime, theme);

        return reservationRepository.save(reservation);
    }

    public void deleteById(final Long id) {
        //연관 객체에 대해 접근할 필요가 없으니까, Lazy로 바꾸었을 때 별도의 트랜잭션 관리가 필요가 없음.
        //그러면 여기서 Reservation이 나오는데, 그 Reservation의 연관 관계를 가지는 애들은 다 null이 들어가 있나?
        //프록시 객체로 감싸져있음.
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 데이터가 존재하지 않습니다. id = " + id));

        reservationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        //연관 객체에 대한 접근이 필요함. 하지만 다른 메서드에서 Lazy덕분에 성능상 이점을 가져오고 있으니까, 해당 부분에서는 JPQL을 사용해서 Eager처럼 사용해야할 것 같음.
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AvailableReservationTime> findAvailableReservationTimes(final LocalDate date, final Long themeId) {
        final List<AvailableReservationTime> availableReservationTimes = new ArrayList<>();
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + themeId));

        for (ReservationTime reservationTime : reservationTimes) {
            availableReservationTimes.add(new AvailableReservationTime(
                    reservationTime.getId(),
                    reservationTime.getStartAt(),
                    reservationRepository.existsByDateAndTimeAndTheme(
                            date,
                            reservationTime,
                            theme
                    ))
            );
        }

        return availableReservationTimes;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByMember(final Member member) {
        //멤버를 기준으로 예약을 찾아서 오는데, 예약 정보에는 여러 연관 관계가 있어서, Reservation을 만들어내려면 연관 관계에 접근을 해야함.
        return reservationRepository.findByMember(member);
    }
}
