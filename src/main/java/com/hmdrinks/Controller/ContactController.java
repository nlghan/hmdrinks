package com.hmdrinks.Controller;

import com.hmdrinks.Request.*;
import com.hmdrinks.Response.CRUDContactResponse;
import com.hmdrinks.Response.CRUDVoucherResponse;
import com.hmdrinks.Response.ListAllContactResponse;
import com.hmdrinks.Response.ListAllVoucherResponse;
import com.hmdrinks.Service.ContactService;
import com.hmdrinks.Service.VoucherService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/contact")
public class ContactController {
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private ContactService contactService;

    @PostMapping(value = "/create")
    public ResponseEntity<CRUDContactResponse> createVoucher(@RequestBody CreateContactReq req){
        return ResponseEntity.ok(contactService.createContact(req));
    }

    @GetMapping(value ="/view/{id}")
    public ResponseEntity<CRUDContactResponse> getOneContact(
            @PathVariable Integer id,HttpServletRequest httpRequest
    ){
        return ResponseEntity.ok(contactService.getContactById(id));
    }

    @PutMapping(value = "/update")
    public ResponseEntity<CRUDContactResponse> updateContact(
            @RequestBody CrudContactReq req, HttpServletRequest httpRequest
    )
    {
        return  ResponseEntity.ok(contactService.updateContact(req));
    }

    @GetMapping(value = "/view/all")
    public ResponseEntity<ListAllContactResponse> getAllContact(
            @RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit
    ){

        return  ResponseEntity.ok(contactService.listAllContact(page,limit));
    }

    @GetMapping(value = "/view/all/complete")
    public ResponseEntity<ListAllContactResponse> getAllContactComplete(
            @RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit
    ){

        return  ResponseEntity.ok(contactService.listAllContactComplete(page,limit));
    }

    @GetMapping(value = "/view/all/waiting")
    public ResponseEntity<ListAllContactResponse> getAllContactWaiting(
            @RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit
    ){

        return  ResponseEntity.ok(contactService.listAllContactWaiting(page,limit));
    }

    @PutMapping("/contact/response")
    public  ResponseEntity<?> responseContact(@RequestBody AcceptContactReq req)
    {
        return ResponseEntity.ok(contactService.responseContact(req));
    }

}