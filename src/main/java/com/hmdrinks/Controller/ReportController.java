package com.hmdrinks.Controller;

import com.hmdrinks.Request.ChangePasswordReq;
import com.hmdrinks.Request.UserInfoUpdateReq;
import com.hmdrinks.Response.GetDetailUserInfoResponse;
import com.hmdrinks.Response.UpdateUserInfoResponse;
import com.hmdrinks.Service.ReportService;
import com.hmdrinks.Service.UserService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/total-product")
    public ResponseEntity<?> totalCVApply() {
        return ResponseEntity.ok(reportService.totalProduct());
    }

    @GetMapping("/total-productVariants")
    public ResponseEntity<?> totalInterview() {
        return ResponseEntity.ok(reportService.totalProductVariants());
    }
}