package com.railway;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null ||
            email.isBlank() || password.isBlank()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Email and password are required.\"}"
            );
            return;
        }

        String sql = """
                SELECT u_id, u_name, u_email
                FROM `user`
                WHERE u_email = ? AND password = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    int userId = result.getInt("u_id");
                    String name = result.getString("u_name");
                    String userEmail = result.getString("u_email");

                    response.setStatus(HttpServletResponse.SC_OK);

                    response.getWriter().write(
                        "{\"success\":true," +
                        "\"message\":\"Login successful.\"," +
                        "\"userId\":" + userId + "," +
                        "\"name\":\"" + name + "\"," +
                        "\"email\":\"" + userEmail + "\"}"
                    );

                } else {

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                    response.getWriter().write(
                        "{\"success\":false,\"message\":\"Invalid email or password.\"}"
                    );
                }
            }

        } catch (Exception e) {

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Login failed.\"}"
            );

            e.printStackTrace();
        }
    }
}