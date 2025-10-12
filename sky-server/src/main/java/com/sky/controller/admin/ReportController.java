package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.service.UserService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
@Slf4j
@RestController
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @ApiOperation("营业额统计")
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnOverStatistics
    (
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("正在统计营业额,begin:{},end:{}",begin,end);
        TurnoverReportVO turnoverReportVO = reportService.turnOverStatistics(begin,end);
        return Result.success(turnoverReportVO);
    }

    /**
     * 用户统计
     * @return
     */
    @ApiOperation("用户统计")
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics
    (
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("正在统计用户数据:{}-{}",begin,end);
        UserReportVO userReportVO = reportService.getUserReport(begin,end);
        return Result.success(userReportVO);
    }

    /**
     * 订单统计
     */
    @ApiOperation("订单统计")
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics
    (
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("正在统计订单数据:{}-{}",begin,end);
        OrderReportVO orderReportVO = reportService.getOrderReport(begin,end);
        return Result.success(orderReportVO);
    }

    /**
     * 销量排名top10
     */
    @ApiOperation("销量排名top10")
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> getTop10
    (
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("正在进行销量统计:{}-{}",begin,end);
        SalesTop10ReportVO salesTop10ReportVO = reportService.getTop10(begin,end);
        return Result.success(salesTop10ReportVO);
    }

    /**
     * 获取业务报表
     * @param httpServletResponse
     */
    @ApiOperation("获取业务报表")
    @GetMapping("/export")
    public void report(HttpServletResponse httpServletResponse){
        reportService.report(httpServletResponse);
    }
}
