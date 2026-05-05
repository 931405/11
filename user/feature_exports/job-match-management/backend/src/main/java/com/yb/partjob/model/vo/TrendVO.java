package com.yb.partjob.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendVO {
    private List<String> dates;
    private List<Integer> counts;
}
