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
import java.util.regex.Pattern;
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

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    public static boolean isValidEmail(String email) {
        return email != null && Pattern.matches(EMAIL_REGEX, email);
    }
    public ResponseEntity<?> createContact(CreateContactReq req) {
        if (req.getPhone() == null || req.getPhone().length() != 10) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số điện thoại không hợp lệ. Phải chứa 10 chữ số.");
        }

        if(!isValidEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email không hợp lệ");
        }

        Contact contact = new Contact();
        contact.setDescription(req.getDescription());
        contact.setEmail(req.getEmail());
        contact.setPhoneNumber(req.getPhone());
        contact.setFullName(req.getFullName());
        contact.setStatus(Status_Contact.WAITING);
        contact.setIsDeleted(false);
        contact.setCreateDate(LocalDateTime.now());
        contactRepository.save(contact);
        String to = req.getEmail();
        if(to != null && !to.equals(""))
        {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("HMDRINKS");
            message.setTo(to);
            String text = "Dear " + req.getFullName() + ",\n\n"
                    + "We have successfully received your feedback with the following details:\n\n"
                    + "Description: " + req.getDescription() + "\n\n"
                    + "Please do not reply to this message.\n\n"
                    + "Best regards,\n"
                    + "HM Drinks Support Team";

            message.setSubject("Feedback Received Confirmation");
            message.setText(text);
            javaMailSender1.send(message);
            System.out.println("Sending email to: " + to);

        }

        return ResponseEntity.status(HttpStatus.OK).body(new CRUDContactResponse(
                contact.getContactId(),
                contact.getDescription(),
                contact.getStatus(),
                contact.getIsDeleted(),
                contact.getCreateDate(),
                contact.getUpdateDate(),
                contact.getDateDeleted(),
                contact.getFullName(),
                contact.getPhoneNumber(),
                contact.getEmail()
        ));
    }

    public ResponseEntity<?> updateContact(CrudContactReq req){
        Contact contact = contactRepository.findByContactIdAndIsDeletedFalse(req.getContactId());
        if (contact == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contact not found");
        }
        if (req.getPhone() == null || req.getPhone().length() != 10) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số điện thoại không hợp lệ. Phải chứa 10 chữ số.");
        }

        if(!isValidEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email không hợp lệ");
        }
        contact.setDescription(req.getDescription());
        contact.setEmail(req.getEmail());
        contact.setPhoneNumber(req.getPhone());
        contact.setFullName(req.getFullName());
        contact.setUpdateDate(LocalDateTime.now());
        contactRepository.save(contact);

        return ResponseEntity.status(HttpStatus.OK).body( new CRUDContactResponse(
                contact.getContactId(),
                contact.getDescription(),
                contact.getStatus(),
                contact.getIsDeleted(),
                contact.getCreateDate(),
                contact.getUpdateDate(),
                contact.getDateDeleted(),
                contact.getFullName(),
                contact.getPhoneNumber(),
                contact.getEmail()
        ));
    }

    public ResponseEntity<?> getContactById(int contactId){
        Contact contact = contactRepository.findByContactId(contactId);
        if (contact == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contact not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDContactResponse(
                contact.getContactId(),
                contact.getDescription(),
                contact.getStatus(),
                contact.getIsDeleted(),
                contact.getCreateDate(),
                contact.getUpdateDate(),
                contact.getDateDeleted(),
                contact.getFullName(),
                contact.getPhoneNumber(),
                contact.getEmail()
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
        String to = contact.getEmail();
        if(to != null && !to.equals(""))
        {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("HMDRINKS");
            message.setTo(to);
            String text = "Dear " + contact.getFullName() + ",\n\n"
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
        List<Contact> contacts1 = contactRepository.findAll();
        List<CRUDContactResponse> responses = new ArrayList<>();
        for(Contact contact : contacts) {
            responses.add(new CRUDContactResponse(
                    contact.getContactId(),
                    contact.getDescription(),
                    contact.getStatus(),
                    contact.getIsDeleted(),
                    contact.getCreateDate(),
                    contact.getUpdateDate(),
                    contact.getDateDeleted(),
                    contact.getFullName(),
                    contact.getPhoneNumber(),
                    contact.getEmail()
            ));
        }
        return new ListAllContactResponse(
                page,
                contacts.getTotalPages(),
                limit,
                contacts1.size(),
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
        List<Contact> contacts1 = contactRepository.findAllByStatus(Status_Contact.WAITING);
        List<CRUDContactResponse> responses = new ArrayList<>();
        int total = 0;
        for(Contact contact : contacts) {
            responses.add(new CRUDContactResponse(
                    contact.getContactId(),
                    contact.getDescription(),
                    contact.getStatus(),
                    contact.getIsDeleted(),
                    contact.getCreateDate(),
                    contact.getUpdateDate(),
                    contact.getDateDeleted(),
                    contact.getFullName(),
                    contact.getPhoneNumber(),
                    contact.getEmail()
            ));
            total++;
        }
        return new ListAllContactResponse(
                page,
                contacts.getTotalPages(),
                limit,
                contacts1.size(),
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
        List<Contact> contacts1 = contactRepository.findAllByStatus(Status_Contact.COMPLETED);
        List<CRUDContactResponse> responses = new ArrayList<>();
        int total = 0;
        for(Contact contact : contacts) {
            responses.add(new CRUDContactResponse(
                    contact.getContactId(),
                    contact.getDescription(),
                    contact.getStatus(),
                    contact.getIsDeleted(),
                    contact.getCreateDate(),
                    contact.getUpdateDate(),
                    contact.getDateDeleted(),
                    contact.getFullName(),
                    contact.getPhoneNumber(),
                    contact.getEmail()
            ));
            total++;
        }
        return new ListAllContactResponse(
                page,
                contacts.getTotalPages(),
                limit,
                contacts1.size(),
                responses
        );
    }
}
