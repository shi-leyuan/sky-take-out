package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
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
    @Autowired
    private UserMapper userMapper;

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

    /**
     * 统计指定时间范围的用户
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        //dateList存放开始日期到结束日期的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //newUserList存放每天新增的用户数量
        List<Integer> newUserList = new ArrayList<>();

        //totalUserList存放总用户数
        List<Integer> totalUserList = new ArrayList<>();


        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//当前开始时间，带时分秒
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//当前结束时间，带时分秒

            Map map = new HashMap();
            map.put("end", endTime);

            //总用户数量
            Integer totalUser = userMapper.countByMap(map);
            map.put("begin", beginTime);
            //新增用户数量
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStastistic(LocalDate begin, LocalDate end) {

        //dateList存放开始日期到结束日期的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //orderCountList存放每天订单总数
        List<Integer> orderCountList = new ArrayList<>();

        //validOrderList存放每天有效订单数
        List<Integer> validOrderList = new ArrayList<>();

        //遍历dateList，查询每天有效订单数和订单总数
        for (LocalDate date : dateList) {
            //查询每天订单总数
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//当前开始时间，带时分秒
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//当前结束时间，带时分秒
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            //查询每天有效订单数
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderList.add(validOrderCount);
        }

        //计算区间内订单总数（stream流更优雅）
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算区间内有效订单总数
        Integer validOrderCount = validOrderList.stream().reduce(Integer::sum).get();
        //计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }


        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 根据订单状态统计数量
     *
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);

        return orderMapper.countByMap(map);
    }
}
