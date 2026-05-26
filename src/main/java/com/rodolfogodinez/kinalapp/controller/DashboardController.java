package com.rodolfogodinez.kinalapp.controller;

import com.rodolfogodinez.kinalapp.entity.Cliente;
import com.rodolfogodinez.kinalapp.entity.Producto;
import com.rodolfogodinez.kinalapp.entity.Ventas;
import com.rodolfogodinez.kinalapp.service.IClienteService;
import com.rodolfogodinez.kinalapp.service.IProductoService;
import com.rodolfogodinez.kinalapp.service.IUsuarioService;
import com.rodolfogodinez.kinalapp.service.IVentasService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class DashboardController {

    private final IClienteService clienteService;
    private final IProductoService productoService;
    private final IVentasService ventasService;
    private final IUsuarioService usuarioService;

    public DashboardController(IClienteService clienteService,
                               IProductoService productoService,
                               IVentasService ventasService,
                               IUsuarioService usuarioService) {
        this.clienteService = clienteService;
        this.productoService = productoService;
        this.ventasService = ventasService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(value = "seccion", defaultValue = "clientes") String seccion,
                            Model model,
                            @AuthenticationPrincipal UserDetails userDetails) {

        // Si un USER intenta acceder a la sección usuarios, redirigir a clientes
        boolean isAdmin = userDetails != null &&
                userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if ("usuarios".equals(seccion) && !isAdmin) {
            return "redirect:/?seccion=clientes";
        }

        model.addAttribute("seccionActual", seccion);

        if (userDetails != null) {
            usuarioService.buscarPorUsername(userDetails.getUsername()).ifPresent(u ->
                    model.addAttribute("codigoUsuarioActual", u.getCodigoUsuario())
            );
        }

        List<Cliente> clientes = clienteService.listarTodos();
        List<Producto> productos = productoService.listarTodos();
        List<Ventas> ventas = ventasService.listarTodos();

        model.addAttribute("listaClientes", clientes);
        model.addAttribute("listaProductos", productos);
        model.addAttribute("listaVentas", ventas);

        // Siempre proveer los formularios (tanto ADMIN como USER pueden agregar)
        if (seccion.equals("clientes")) {
            if (!model.containsAttribute("clienteForm")) {
                model.addAttribute("clienteForm", new Cliente());
            }
        } else if (seccion.equals("productos")) {
            if (!model.containsAttribute("productoForm")) {
                model.addAttribute("productoForm", new Producto());
            }
        } else if (seccion.equals("usuarios") && isAdmin) {
            model.addAttribute("listaUsuarios", usuarioService.listarTodos());
        }

        return "dashboard";
    }

    // Cualquier usuario autenticado puede guardar clientes
    @PostMapping("/guardarCliente")
    public String guardarCliente(@ModelAttribute Cliente cliente,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (cliente.getEstado() == null) {
                cliente.setEstado(1);
            }
            clienteService.guardar(cliente);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cliente guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error: " + e.getMessage());
        }
        return "redirect:/?seccion=clientes";
    }

    // Cualquier usuario autenticado puede guardar productos
    @PostMapping("/guardarProducto")
    public String guardarProducto(@ModelAttribute Producto producto,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (producto.getEstado() == null) {
                producto.setEstado(1);
            }
            if (producto.getStock() == null) {
                producto.setStock(0);
            }
            productoService.guardar(producto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error: " + e.getMessage());
        }
        return "redirect:/?seccion=productos";
    }

    // Solo ADMIN puede editar clientes (botón editar en tabla)
    @GetMapping("/clientes/editar")
    public String editarCliente(@RequestParam String dpi,
                                RedirectAttributes redirectAttributes,
                                @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails != null &&
                userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return "redirect:/?seccion=clientes";
        }
        clienteService.buscarPorDPI(dpi).ifPresent(cliente ->
                redirectAttributes.addFlashAttribute("clienteForm", cliente)
        );
        return "redirect:/?seccion=clientes";
    }

    // Solo ADMIN puede editar productos
    @GetMapping("/productos/editar")
    public String editarProducto(@RequestParam Long codigo,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails != null &&
                userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return "redirect:/?seccion=productos";
        }
        productoService.buscarPorCodigo(codigo).ifPresent(producto ->
                redirectAttributes.addFlashAttribute("productoForm", producto)
        );
        return "redirect:/?seccion=productos";
    }
}
