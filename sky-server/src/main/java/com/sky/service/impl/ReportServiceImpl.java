package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;


    /**
     * 统计指定时间范围的营业额
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        //dateList存放开始日期到结束日期的每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        //turnoverList存放当前日期的营业额
        List<Double> turnoverList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //转为字符串，为了后续封装vo对象
        String stringDate = StringUtils.join(dateList, ",");

        for (LocalDate date : dateList) {
            //查询date日期对应的营业额
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//当前开始时间，带时分秒
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//当前结束时间，带时分秒
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.subByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        //转为字符串，为了后续封装vo对象
        String stringTurnover = StringUtils.join(turnoverList, ",");

        return TurnoverReportVO.builder()
                .dateList(stringDate)
                .turnoverList(stringTurnover)
                .build();
    }
}
