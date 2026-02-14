package sport.store.thinh.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResBrandDTO {
    private long id;
    private String name;
    private String slug;
    private String description;
    private String image;
    private String country;
}
