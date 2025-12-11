package org.example.hrm.common;

import java.util.List;

import lombok.Data;

@Data
public class PageResult<T> {
      // 当前页码
    private Integer pageNum;
    // 每页数量
    private Integer pageSize;
    // 总记录数
    private Long total;
    // 总页数
    private Integer totalPages;
    // 数据列表
    private List<T> list;
    
    public PageResult() {}
    
    public PageResult(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
        this.list = list;
    }

     public static <T> PageResult<T> success(Integer pageNum, Integer pageSize, 
                                           Long total, List<T> list) {
        return new PageResult<>(pageNum, pageSize, total, list);
    }

}
