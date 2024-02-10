package com.balram.spring.controller;

import com.balram.spring.model.Employee;
import com.balram.spring.repo.EmpRepo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@Slf4j
public class JobController {

    @Autowired
    Job asyncBatchJob;

    @Autowired
    Job singleThreadedJob;

    @Autowired
    JobLauncher asyncJobLauncher;

    @Autowired EmpRepo repo;

    @PostConstruct
    public void doDemo(){
        Employee employee = new Employee("Name","Role");
        Employee save = repo.save(employee);
        log.info(save.getName() + " : "+save.getId());
    }

    @GetMapping(value = "asyncBatchJob")
    public void batch() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("currentDate", new Date())
                .addString("inputFileName", "transactions.csv")
                .toJobParameters();
        asyncJobLauncher.run(asyncBatchJob, jobParameters);
    }


    @GetMapping(value = "singleThreadedJob")
    public void singleThreadedJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("currentDate", new Date())
                .addString("inputFileName", "transactions.csv")
                .toJobParameters();
        asyncJobLauncher.run(singleThreadedJob, jobParameters);
    }
}
