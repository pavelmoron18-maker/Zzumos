package com.zzumos.controller;

import com.zzumos.dto.Dtos.*;
import com.zzumos.model.enums.EstadoPedido;
import com.zzumos.security.UsuarioPrincipal;
import com.zzumos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService service;

    public PedidoController(PedidoService service) { this.service = service; }

    /** CLIENTE: crea su pedido. El cliente se toma de la sesión. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse crear(@Valid @RequestBody CrearPedidoRequest req,
                                @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return service.crear(req, usuario.getIdUsuario());
    }

    /** CLIENTE: historial de sus pedidos. */
    @GetMapping("/mis-pedidos")
    public List<PedidoResponse> misPedidos(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        return service.listarMisPedidos(usuario.getIdUsuario());
    }

    /** PERSONAL / ADMINISTRADOR: todos los pedidos, con filtro opcional por estado. */
    @GetMapping
    public List<PedidoResponse> listar(@RequestParam(required = false) EstadoPedido estado) {
        return service.listar(estado);
    }

    /** Seguimiento por código. Un CLIENTE solo ve los suyos. */
    @GetMapping("/{codigo}")
    public PedidoResponse seguimiento(@PathVariable String codigo,
                                      @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return service.buscarPorCodigo(codigo, usuario);
    }

    /** PERSONAL / ADMINISTRADOR: avanza el estado. Queda registrado quién lo atendió. */
    @PatchMapping("/{id}/estado")
    public PedidoResponse cambiarEstado(@PathVariable Integer id,
                                        @Valid @RequestBody CambiarEstadoRequest req,
                                        @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return service.cambiarEstado(id, req, usuario.getIdUsuario());
    }
}
