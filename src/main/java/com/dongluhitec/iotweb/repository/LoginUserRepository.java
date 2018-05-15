package com.dongluhitec.iotweb.repository;

import com.dongluhitec.iotweb.bean.LoginUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginUserRepository extends CrudRepository<LoginUser, Long> {

}
