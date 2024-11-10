package com.hmdrinks.Service;
import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Status_Contact;
import com.hmdrinks.Enum.Status_Voucher;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.ContactRepository;
import com.hmdrinks.Repository.PostRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Repository.VoucherRepository;
import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContactService {
    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSender javaMailSender1;

    public ResponseEntity<?> createContact(CreateContactReq req) {
        User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Contact contact = new Contact();
        contact.setDescription(req.getDescription());
        contact.setUser(user);
        contact.setStatus(Status_Contact.WAITING);
        contact.setIsDeleted(false);
        contact.setCreateDate(LocalDateTime.now());
        contactRepository.save(contact);
        String to = user.getEmail();
        if(to != null && !to.equals(""))
        {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("HMDRINKS");
            message.setTo(to);
            String text = "Dear " + user.getFullName() + ",\n\n"
                    + "We have successfully received your feedback with the following details:\n\n"
                    + "Description: " + req.getDescription() + "\n\n"
                    + "Please do not reply to this message.\n\n"
                    + "Best regards,\n"
                    + "HM Drinks Support Team";

            message.setSubject("Feedback Received Confirmation");
            message.setText(text);
            javaMailSender1.send(message);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new CRUDContactResponse(
                contact.getContactId(),
                contact.getUser().getUserId(),
                contact.getDescription(),
                contact.getStatus(),
                contact.getIsDeleted(),
                contact.getCreateDate(),
                contact.getUpdateDate(),
                contact.getDateDeleted()
        ));
    }

    public ResponseEntity<?> updateContact(CrudContactReq req){
        Contact contact = contactRepository.findByContactIdAndUserUserIdAndIsDeletedFalse(req.getContactId(), req.getUserId());
        if (contact == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contact not found");
        }
        contact.setDescription(req.getDescription());
        contact.setUser(contact.getUser());
        contact.setUpdateDate(LocalDateTime.now());
        contactRepository.save(contact);

        return ResponseEntity.status(HttpStatus.OK).body( new CRUDContactResponse(
                contact.getContactId(),
                contact.getUser().getUserId(),
                contact.getDescription(),
                contact.getStatus(),
                contact.getIsDeleted(),
                contact.getCreateDate(),
                contact.getUpdateDate(),
                contact.getDateDeleted()
        ));
    }

    public ResponseEntity<?> getContactById(int contactId){
        Contact contact = contactRepository.findByContactId(contactId);
        if (contact == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contact not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDContactResponse(
                contact.getContactId(),
                contact.getUser().getUserId(),
                contact.getDescription(),
                contact.getStatus(),
                contact.getIsDeleted(),
                contact.getCreateDate(),
                contact.getUpdateDate(),
                contact.getDateDeleted()
        ));
    }

    public String deleteContact(int contactId){
        Contact contact = contactRepository.findByContactId(contactId);
        if (contact == null) {
            throw new BadRequestException("Not found Contact");
        }
        contact.setIsDeleted(true);
        contactRepository.save(contact);
        return "Deleted Contact";

    }



    public ResponseEntity<?> responseContact(AcceptContactReq req){
        Contact contact = contactRepository.findByContactId(req.getContactId());
        if (contact == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contact not found");
        }
        User user = userRepository.findByUserId(contact.getUser().getUserId());
        String to = user.getEmail();
        if(to != null && !to.equals(""))
        {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("HMDRINKS");
            message.setTo(to);
            String text = "Dear user,\n\n"
                    +  req.getContent()
                    + "\n\n"
                    + "Please do not reply to this message.\n\n"
                    + "Best regards,\n"
                    + "Your Application Support Team";
            message.setSubject("Response Contact");
            message.setText(text);
            javaMailSender1.send(message);
        }
        contact.setUpdateDate(LocalDateTime.now());
        contact.setStatus(Status_Contact.COMPLETED);
        contactRepository.save(contact);
        return ResponseEntity.status(HttpStatus.OK).body("Response success");
    }

    public ListAllContactResponse listAllContact(String pageFromParam, String limitFromParam)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Contact> contacts = contactRepository.findAll(pageable);
        List<CRUDContactResponse> responses = new ArrayList<>();
        for(Contact contact : contacts) {
            responses.add(new CRUDContactResponse(
                    contact.getContactId(),
                    contact.getUser().getUserId(),
                    contact.getDescription(),
                    contact.getStatus(),
                    contact.getIsDeleted(),
                    contact.getCreateDate(),
                    contact.getUpdateDate(),
                    contact.getDateDeleted()
            ));
        }
        return new ListAllContactResponse(
                page,
                contacts.getTotalPages(),
                limit,
                responses
        );
    }

    public ListAllContactResponse listAllContactWaiting(String pageFromParam, String limitFromParam)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Contact> contacts = contactRepository.findAllByStatus(Status_Contact.WAITING, pageable);
        List<CRUDContactResponse> responses = new ArrayList<>();
        for(Contact contact : contacts) {
            responses.add(new CRUDContactResponse(
                    contact.getContactId(),
                    contact.getUser().getUserId(),
                    contact.getDescription(),
                    contact.getStatus(),
                    contact.getIsDeleted(),
                    contact.getCreateDate(),
                    contact.getUpdateDate(),
                    contact.getDateDeleted()
            ));
        }
        return new ListAllContactResponse(
                page,
                contacts.getTotalPages(),
                limit,
                responses
        );
    }

    public ListAllContactResponse listAllContactComplete(String pageFromParam, String limitFromParam)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Contact> contacts = contactRepository.findAllByStatus(Status_Contact.COMPLETED, pageable);
        List<CRUDContactResponse> responses = new ArrayList<>();
        for(Contact contact : contacts) {
            responses.add(new CRUDContactResponse(
                    contact.getContactId(),
                    contact.getUser().getUserId(),
                    contact.getDescription(),
                    contact.getStatus(),
                    contact.getIsDeleted(),
                    contact.getCreateDate(),
                    contact.getUpdateDate(),
                    contact.getDateDeleted()
            ));
        }
        return new ListAllContactResponse(
                page,
                contacts.getTotalPages(),
                limit,
                responses
        );
    }
}