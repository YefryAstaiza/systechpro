package com.systechpro.controllers;

import com.systechpro.models.Rol;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(name = "AdministrativoController", urlPatterns = {"/api/administrativo/*"})
public class AdministrativoController extends SolicitanteBaseController {
    @Override
    protected Rol rolPermitido() {
        return Rol.ADMINISTRATIVO;
    }
}
