package com.uca.parcialfinalncapas.controller;

import com.uca.parcialfinalncapas.dto.request.TicketCreateRequest;
import com.uca.parcialfinalncapas.dto.request.TicketUpdateRequest;
import com.uca.parcialfinalncapas.dto.response.GeneralResponse;
import com.uca.parcialfinalncapas.dto.response.TicketResponse;
import com.uca.parcialfinalncapas.dto.response.TicketResponseList;
import com.uca.parcialfinalncapas.exceptions.BadTicketRequestException;
import com.uca.parcialfinalncapas.service.TicketService;
import com.uca.parcialfinalncapas.repository.UserRepository;
import com.uca.parcialfinalncapas.repository.TicketRepository;
import com.uca.parcialfinalncapas.utils.ResponseBuilderUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@AllArgsConstructor
public class TicketController {
    private TicketService ticketService;
    private UserRepository userRepository;
    private TicketRepository ticketRepository;

    @GetMapping
    public ResponseEntity<GeneralResponse> getAllTickets(Authentication authentication) {
        var tickets = ticketService.getAllTickets();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER"))) {
            var user = userRepository.findByCorreo(authentication.getName());
            if (user.isPresent()) {
                tickets = tickets.stream()
                        .filter(t -> t.getSolicitanteId().equals(user.get().getId()))
                        .toList();
            }
        }
        return ResponseBuilderUtil.buildResponse("Tickets obtenidos correctamente",
                tickets.isEmpty() ? HttpStatus.BAD_REQUEST : HttpStatus.OK,
                tickets);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TECH') or hasAuthority('USER')")
    public ResponseEntity<GeneralResponse> getTicketById(@PathVariable Long id, Authentication authentication) {
        TicketResponse ticket = ticketService.getTicketById(id);
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER"))) {
            var user = userRepository.findByCorreo(authentication.getName());
            var ticketEntity = ticketRepository.findById(id);
            if (user.isPresent() && ticketEntity.isPresent() && !ticketEntity.get().getUsuarioId().equals(user.get().getId())) {
                return ResponseBuilderUtil.buildResponse("Forbidden", HttpStatus.FORBIDDEN, null);
            }
        }
        if (ticket == null) {
            throw new BadTicketRequestException("Ticket no encontrado");
        }
        return ResponseBuilderUtil.buildResponse("Ticket found", HttpStatus.OK, ticket);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<GeneralResponse> createTicket(@Valid @RequestBody TicketCreateRequest ticket) {
        TicketResponse createdTicket = ticketService.createTicket(ticket);
        return ResponseBuilderUtil.buildResponse("Ticket creado correctamente", HttpStatus.CREATED, createdTicket);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('TECH')")
    public ResponseEntity<GeneralResponse> updateTicket(@Valid @RequestBody TicketUpdateRequest ticket) {
        TicketResponse updatedTicket = ticketService.updateTicket(ticket);
        return ResponseBuilderUtil.buildResponse("Ticket actualizado correctamente", HttpStatus.OK, updatedTicket);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TECH')")
    public ResponseEntity<GeneralResponse> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseBuilderUtil.buildResponse("Ticket eliminado correctamente", HttpStatus.OK, null);
    }
}
