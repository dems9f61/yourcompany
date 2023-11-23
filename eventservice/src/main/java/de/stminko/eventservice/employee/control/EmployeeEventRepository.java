package de.stminko.eventservice.employee.control;

import de.stminko.eventservice.employee.entity.PersistentEmployeeEvent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeEventRepository extends MongoRepository<PersistentEmployeeEvent, String> {

	Page<PersistentEmployeeEvent> findByEmployeeId(String employeeId, Pageable pageable);

}
