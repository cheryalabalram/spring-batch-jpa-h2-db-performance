package com.balram.spring.batch.controller;

import com.balram.spring.batch.model.Employee;
import com.balram.spring.batch.repo.EmpRepo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class EmpController {

    @Autowired EmpRepo repo;

    @PostConstruct
    public void doDemo(){
        Employee employee = new Employee("Name","Role");
        Employee save = repo.save(employee);
        log.info(save.getName() + " : "+save.getId());
    }
}
