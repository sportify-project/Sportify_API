package sport.store.thinh.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCategoryDTO {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private Integer displayOrder;

    private Long parentId;
}
