package com.energypulse.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.energypulse.backend.model.Contacts;

@Repository 
public interface ContactRepository extends JpaRepository<Contacts, Long> {

}
