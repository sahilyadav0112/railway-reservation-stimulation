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

@WebServlet("/train-classes")
public class TrainClassesServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String trainId = request.getParameter("trainId");

        if (trainId == null || trainId.isBlank()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Train ID is required.\"}"
            );

            return;
        }

        String sql = """
                SELECT
                    tc.c_id,
                    tc.c_no,
                    tc.c_name,
                    tc.base_fare,
                    c.coach_id,
                    c.coach_number,
                    c.total_seats,
                    COUNT(
                        CASE
                            WHEN s.status = 'AVAILABLE'
                            THEN 1
                        END
                    ) AS available_seats
                FROM train_classes tc
                JOIN coaches c
                    ON tc.c_id = c.class_id
                LEFT JOIN seat s
                    ON c.coach_id = s.coach_id
                WHERE c.t_id = ?
                GROUP BY
                    tc.c_id,
                    tc.c_no,
                    tc.c_name,
                    tc.base_fare,
                    c.coach_id,
                    c.coach_number,
                    c.total_seats
                ORDER BY tc.c_id, c.coach_id
                """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {

            statement.setInt(1, Integer.parseInt(trainId));

            try (ResultSet result = statement.executeQuery()) {

                StringBuilder json = new StringBuilder();

                json.append("{\"success\":true,\"classes\":[");

                boolean first = true;

                while (result.next()) {

                    if (!first) {
                        json.append(",");
                    }

                    first = false;

                    json.append("{");

                    json.append("\"classId\":")
                        .append(result.getInt("c_id"))
                        .append(",");

                    json.append("\"classCode\":\"")
                        .append(escapeJson(result.getString("c_no")))
                        .append("\",");

                    json.append("\"className\":\"")
                        .append(escapeJson(result.getString("c_name")))
                        .append("\",");

                    json.append("\"fare\":")
                        .append(result.getBigDecimal("base_fare"))
                        .append(",");

                    json.append("\"coachId\":")
                        .append(result.getInt("coach_id"))
                        .append(",");

                    json.append("\"coachNumber\":\"")
                        .append(escapeJson(result.getString("coach_number")))
                        .append("\",");

                    json.append("\"totalSeats\":")
                        .append(result.getInt("total_seats"))
                        .append(",");

                    json.append("\"availableSeats\":")
                        .append(result.getInt("available_seats"));

                    json.append("}");
                }

                json.append("]}");

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(json.toString());
            }

        } catch (NumberFormatException e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid train ID.\"}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to load train classes.\"}"
            );
        }
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}