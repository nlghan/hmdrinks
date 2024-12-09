package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Contact;
import com.hmdrinks.Entity.Voucher;
import com.hmdrinks.Enum.Status_Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

    Contact findByContactId(int contactId);
    Contact findByContactIdAndIsDeletedFalse(int contactId);
    List<Contact> findByIsDeletedFalse();
    Page<Contact> findAllByStatus(Status_Contact status,Pageable pageable);
    List<Contact> findAllByStatus(Status_Contact status);
    Page<Contact> findAll(Pageable pageable);
}
