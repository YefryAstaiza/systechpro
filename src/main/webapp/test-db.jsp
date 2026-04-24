<%@ page import="com.systechpro.utils.GestorJDBC" %>
<%@ page import="java.sql.Connection" %>
<%@ page contentType="application/json" %>
<%
    try {
        Connection conn = GestorJDBC.getConnection();
        if (conn != null) {
            out.print("{\"status\": \"success\", \"message\": \"Conexión a base de datos exitosa\"}");
            conn.close();
        }
    } catch (Exception e) {
        out.print("{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\", \"cause\": \"" + e.getCause() + "\"}");
    }
%>