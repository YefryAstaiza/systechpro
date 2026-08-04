package com.systechpro.controllers;

import com.systechpro.models.Rol;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(name = "DocenteController", urlPatterns = {"/api/docente/*"})
public class DocenteController extends SolicitanteBaseController {
    @Override
    protected Rol rolPermitido() {
        return Rol.DOCENTE;
    }
}
