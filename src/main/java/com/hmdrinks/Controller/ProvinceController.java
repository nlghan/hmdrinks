package com.hmdrinks.Controller;

import com.hmdrinks.Service.ProvinceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/province")
@RequiredArgsConstructor
public class ProvinceController {
    @Autowired
    private ProvinceService provinceService;

    @GetMapping(value = "/listAll")
    public ResponseEntity<?> listAllCartItem(

    ) {
        return provinceService.fetchProvinces();
    }

        @GetMapping("/list-district")
    public  ResponseEntity<?> getAllDistrict(@RequestParam Integer provinceId){
        return provinceService.fetchDistricts(provinceId.toString());
    }

    @GetMapping("/list-ward")
    public  ResponseEntity<?> getAllWard(@RequestParam Integer districtId){
        return provinceService.fetchWard(districtId.toString());
    }
}