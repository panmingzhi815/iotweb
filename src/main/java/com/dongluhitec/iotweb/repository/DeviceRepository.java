package com.dongluhitec.iotweb.repository;

import com.dongluhitec.iotweb.bean.CommandRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends CrudRepository<CommandRes, Long> {

    Page<CommandRes> findAll(Specification<CommandRes> specification, Pageable page);

}
