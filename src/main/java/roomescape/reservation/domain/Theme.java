package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String themeName;
    private String description;
    private String thumbnail;

    public Theme() {
    }

    public Theme(String themeName, String description, String thumbnail) {
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(Long id, String themeName, String description, String thumbnail) {
        this.id = id;
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public List<Theme> getPopularTheme(List<Theme> themes) {
        Map<Theme, Integer> countTheme = new HashMap<>();
        for (Theme theme : themes) {
            countTheme.put(theme, countTheme.getOrDefault(theme, 0) + 1);
        }
        return sortThemes(countTheme).stream()
                .limit(10)
                .toList();
    }

    private List<Theme> sortThemes(Map<Theme, Integer> countTheme) {
        List<Entry<Theme, Integer>> list = new ArrayList<>(countTheme.entrySet());
        list.sort(Entry.comparingByValue(Comparator.reverseOrder()));

        return list.stream()
                .map(Entry::getKey)
                .toList();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return themeName;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

}
