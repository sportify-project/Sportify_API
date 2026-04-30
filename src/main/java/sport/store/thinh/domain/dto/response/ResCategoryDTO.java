package sport.store.thinh.domain.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResCategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private Integer displayOrder;
    private Long parentId;
    private String parentName;
    private List<ResCategoryDTO> children;
}
