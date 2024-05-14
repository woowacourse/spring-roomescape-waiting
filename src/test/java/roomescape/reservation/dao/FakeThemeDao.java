package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ThemeRepository;

public class FakeThemeDao implements ThemeRepository {
    private final Map<Long, Theme> themes = new HashMap<>();
    private final MemberReservationRepository memberReservationRepository;

    public FakeThemeDao(MemberReservationRepository memberReservationRepository) {
        this.memberReservationRepository = memberReservationRepository;
    }

    @Override
    public List<Theme> findAll() {
        return themes.values().stream()
                .toList();
    }

    @Override
    public Theme save(Theme theme) {
        themes.put((long) themes.size() + 1, theme);
        return new Theme((long) themes.size(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public void deleteById(long themeId) {
        themes.remove(themeId);
    }

    @Override
    public Optional<Theme> findById(long themeId) {
        return Optional.of(themes.get(themeId));
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit) {

        List<MemberReservation> memberReservations = memberReservationRepository.findBy(null, null, startDate, endDate);

        Map<Long, Long> themeReservationCounts = memberReservations.stream()
                .filter(reservationMember -> reservationMember.getReservation().getDate().isAfter(startDate)
                        && reservationMember.getReservation().getDate().isBefore(endDate))
                .collect(Collectors.groupingBy(
                        reservationMember -> reservationMember.getReservation().getTheme().getId(),
                        Collectors.counting()
                ));

        List<Long> popularThemeIds = themeReservationCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();

        return popularThemeIds.stream()
                .map(themes::get)
                .collect(Collectors.toList());
    }
}
