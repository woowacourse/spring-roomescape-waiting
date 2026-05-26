package roomescape.repository;

import static roomescape.repository.FakeReservationRepository.RESERVATION_TABLE;
import static roomescape.repository.FakeReservationTimeRepository.RESERVATION_TIME_TABLE;
import static roomescape.repository.FakeThemeRepository.THEME_TABLE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeDatabase {

    private final Map<String, Map<Long, Object>> database = new HashMap<>();

    public FakeDatabase() {
        database.put(RESERVATION_TIME_TABLE, new HashMap<>());
        database.put(THEME_TABLE, new HashMap<>());
        database.put(RESERVATION_TABLE, new HashMap<>());
    }

    public void create(String tableName, Long id, Object object) {
        database.get(tableName).put(id, object);
    }

    public <T> T read(String tableName, Long id, Class<T> type) {
        return type.cast(database.get(tableName).get(id));
    }

    public <T> List<T> readAll(String tableName, Class<T> type) {
        return List.copyOf(database.get(tableName).values().stream()
                .map(type::cast)
                .toList());
    }

    public void delete(String tableName, Long id) {
        database.get(tableName).remove(id);
    }
}
