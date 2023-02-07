package ru.alishev.springcourse.FirstRestApp.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.alishev.springcourse.FirstRestApp.dto.PersonDto;
import ru.alishev.springcourse.FirstRestApp.models.Person;
import ru.alishev.springcourse.FirstRestApp.services.PeopleService;
import ru.alishev.springcourse.FirstRestApp.util.PersonErrorResponse;
import ru.alishev.springcourse.FirstRestApp.util.PersonNotCreatedException;
import ru.alishev.springcourse.FirstRestApp.util.PersonNotFoundException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping( "/people" )
public class PeopleController {

    private final PeopleService peopleService;
    private final ModelMapper modelMapper;


    @Autowired
    public PeopleController(PeopleService peopleService, ModelMapper modelMapper) {
        this.peopleService = peopleService;
        this.modelMapper = modelMapper;
    }

    @GetMapping()
    public List<PersonDto> getPeople() {
        return peopleService.findAll().stream().map(this::convertToPersonDto).collect(Collectors.toList());
    }

    @GetMapping( "/{id}" )
    public PersonDto getPerson(@PathVariable( "id" ) int id) {
        return convertToPersonDto(peopleService.findOne(id));
    }

    @PostMapping
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid PersonDto personDto,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMsg.append(error.getField())
                        .append(" - ").append(error.getDefaultMessage())
                        .append(";");
            }
            throw new PersonNotCreatedException(errorMsg.toString());
        }

        peopleService.save(convertToPerson(personDto));
        return ResponseEntity.ok(HttpStatus.OK);
    }


    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handleException(PersonNotFoundException e) {
        PersonErrorResponse response = new PersonErrorResponse(
                "person with this id wasn't found!",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handleException(PersonNotCreatedException e) {
        PersonErrorResponse response = new PersonErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private Person convertToPerson(PersonDto personDto) {
        return modelMapper.map(personDto, Person.class);
    }

    private PersonDto convertToPersonDto(Person person) {
        return modelMapper.map(person, PersonDto.class);
    }
}

