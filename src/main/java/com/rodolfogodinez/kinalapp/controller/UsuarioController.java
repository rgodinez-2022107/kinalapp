package com.rodolfogodinez.kinalapp.controller;

import com.rodolfogodinez.kinalapp.entity.Usuario;
import com.rodolfogodinez.kinalapp.service.IUsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final IUsuarioService usuarioService;

    public UsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Usuario>> listarActivos() {
        return ResponseEntity.ok(usuarioService.listarActivos());
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Usuario> buscarPorCodigo(@PathVariable Long codigo) {
        return usuarioService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Usuario usuario) {
        try {
            Usuario nuevo = usuarioService.guardar(usuario);
            return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<?> actualizar(@PathVariable Long codigo, @RequestBody Usuario usuario) {
        try {
            if (!usuarioService.existePorCodigo(codigo)) {
                return ResponseEntity.notFound().build();
            }
            Usuario actualizado = usuarioService.actualizar(codigo, usuario);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{codigo}/estado")
    public ResponseEntity<?> toggleEstado(@PathVariable Long codigo,
                                          @RequestParam Integer estado) {
        try {
            Usuario usuario = usuarioService.buscarPorCodigo(codigo)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            usuario.setEstado(estado);
            Usuario actualizado = usuarioService.actualizar(codigo, usuario);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(@PathVariable Long codigo) {
        try {
            if (!usuarioService.existePorCodigo(codigo)) {
                return ResponseEntity.notFound().build();
            }
            usuarioService.eliminar(codigo);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}