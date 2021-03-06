package com.gshhris.app.service.impl;

import com.gshhris.app.domain.Employee;
import com.gshhris.app.domain.User;
import com.gshhris.app.repository.EmployeeRepository;
import com.gshhris.app.repository.UserRepository;
import com.gshhris.app.service.EmployeeService;
import com.gshhris.app.service.MailService;
import com.gshhris.app.service.UserService;
import com.gshhris.app.service.dto.AdminUserDTO;
import com.gshhris.app.service.dto.EmployeeDTO;
import com.gshhris.app.service.mapper.EmployeeMapper;
import com.gshhris.app.web.rest.errors.BadRequestAlertException;
import com.gshhris.app.web.rest.errors.EmailAlreadyUsedException;
import com.gshhris.app.web.rest.errors.LoginAlreadyUsedException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Employee}.
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;

    private final EmployeeMapper employeeMapper;

    private final UserService userService;

    private final UserRepository userRepository;

    private final MailService mailService;

    public EmployeeServiceImpl(
        EmployeeRepository employeeRepository,
        EmployeeMapper employeeMapper,
        UserService userService,
        UserRepository userRepository,
        MailService mailService
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.userService = userService;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    @Override
    public EmployeeDTO save(EmployeeDTO employeeDTO) {
        log.debug("Request to save Employee : {}", employeeDTO);

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin(employeeDTO.getUsername());
        userDTO.setEmail(employeeDTO.getEmail());

        if (userDTO.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists");
            // Lowercase the user login before comparing with database
        } else if (userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).isPresent()) {
            throw new LoginAlreadyUsedException();
        } else if (userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException();
        } else {
            User newUser = userService.createUser(userDTO);

            Employee employee = employeeMapper.toEntity(employeeDTO);
            employee.setUser(newUser); //to map user to employee
            employee = employeeRepository.save(employee);

            mailService.sendCreationEmail(newUser);

            return employeeMapper.toDto(employee);
        }
    }

    @Override
    public EmployeeDTO update(EmployeeDTO employeeDTO) {
        log.debug("Request to update Employee : {}", employeeDTO);

        Employee employee = employeeMapper.toEntity(employeeDTO);
        employee = employeeRepository.save(employee);

        return employeeMapper.toDto(employee);
    }

    @Override
    public Optional<EmployeeDTO> partialUpdate(EmployeeDTO employeeDTO) {
        log.debug("Request to partially update Employee : {}", employeeDTO);

        return employeeRepository
            .findById(employeeDTO.getId())
            .map(existingEmployee -> {
                employeeMapper.partialUpdate(existingEmployee, employeeDTO);

                return existingEmployee;
            })
            .map(employeeRepository::save)
            .map(employeeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Employees");
        return employeeRepository.findAll(pageable).map(employeeMapper::toDto);
    }

    public Page<EmployeeDTO> findAllWithEagerRelationships(Pageable pageable) {
        return employeeRepository.findAllWithEagerRelationships(pageable).map(employeeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeDTO> findOne(Long id) {
        log.debug("Request to get Employee : {}", id);
        return employeeRepository.findOneWithEagerRelationships(id).map(employeeMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Employee : {}", id);
        employeeRepository.deleteById(id);
    }
}
