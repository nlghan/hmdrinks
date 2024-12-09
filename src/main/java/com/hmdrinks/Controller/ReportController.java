package com.hmdrinks.Controller;

import com.hmdrinks.Service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/day")
    public ResponseEntity<?> reportRevenueByDay(@RequestParam("date") String dateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate date = LocalDate.parse(dateString, formatter);
            return reportService.reportRevenueByDay(date);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use d/M/yyyy.");
        }
    }

    @GetMapping("/month")
    public ResponseEntity<?> reportRevenueByMonth(@RequestParam("year") int year, @RequestParam("month") int month) {
        return reportService.reportRevenueByMonth(year, Month.of(month));
    }

    @GetMapping("/quarter")
    public ResponseEntity<?> reportRevenueByQuarter(@RequestParam("year") int year, @RequestParam("quarter") int quarter) {
        return reportService.reportRevenueByQuarter(year, quarter);
    }

    @GetMapping("/year")
    public ResponseEntity<?> reportRevenueByYear(@RequestParam("year") int year) {
        return reportService.reportRevenueByYear(year);
    }
}
