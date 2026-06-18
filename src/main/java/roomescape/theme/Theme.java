package roomescape.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String thumbnailUrl;

    protected Theme() {}

    private Theme(final Long id, final String name, final String description, final String thumbnailUrl) {
        validateName(name);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 테마 이름은 비어있을 수 없습니다.");
        }
    }

    public static Theme createNew(final String name, final String description, final String thumbnailUrl) {
        return new Theme(null, name, description, thumbnailUrl);
    }

    public static Theme of(final Long id, final String name, final String description, final String thumbnailUrl) {
        validateId(id);
        return new Theme(id, name, description, thumbnailUrl);
    }

    public Theme withId(final Long id) {
        validateId(id);
        return new Theme(id, name, description, thumbnailUrl);
    }

    private static void validateId(final Long id){
        if(id == null) {
            throw new IllegalArgumentException("Id는 비어있을 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Theme)) {
            return false;
        }
        Theme t = (Theme) o;
        return Objects.equals(id, t.getId());
    }

    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

}
