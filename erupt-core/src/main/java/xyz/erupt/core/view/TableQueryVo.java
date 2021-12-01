package xyz.erupt.core.view;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.erupt.annotation.query.Condition;

@Getter
@Setter
@NoArgsConstructor
public class TableQueryVo {

    private static final int maxPageSize = 200;

    private boolean dataExport = false;

    private Integer pageIndex;

    private Integer pageSize;

    private String sort;

    private Object linkTreeVal;

    private List<Condition> condition;
    
    public Integer getPageSize() {
        if (this.isDataExport()) {
            pageSize = Page.PAGE_MAX_DATA;
        } else {
            if (pageSize > maxPageSize) {
                pageSize = maxPageSize;
            }
        }
        return pageSize;
    }

}
