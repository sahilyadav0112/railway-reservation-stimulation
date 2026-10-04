package com.railway;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");

        if (fullName == null || email == null ||
            phone == null || password == null) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"All fields are required.\"}"
            );
            return;
        }

       String sql = """
        INSERT INTO `user` (u_name, u_phone, u_email, password)
        VALUES (?, ?, ?, ?)
        """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fullName);
            statement.setString(2, phone);
            statement.setString(3, email);
            statement.setString(4, password);

            statement.executeUpdate();

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(
                "{\"success\":true,\"message\":\"Registration successful.\"}"
            );

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Registration failed.\"}"
            );

            e.printStackTrace();
        }
    }
}