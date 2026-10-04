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

@WebServlet("/trains")
public class TrainSearchServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String from = request.getParameter("from");
        String to = request.getParameter("to");

        if (from == null || to == null ||
            from.isBlank() || to.isBlank()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"From and To stations are required.\"}"
            );

            return;
        }

        if (from.equalsIgnoreCase(to)) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Source and destination cannot be the same.\"}"
            );

            return;
        }

        String sql = """
                SELECT
                    t.t_id,
                    t.t_name,
                    t.t_no,
                    t.t_type,
                    sf.station_name AS from_station,
                    st.station_name AS to_station,
                    tsf.departure_t AS departure_time,
                    tst.arrival_t AS arrival_time
                FROM trains t
                JOIN train_stations tsf
                    ON t.t_id = tsf.t_id
                JOIN stations sf
                    ON tsf.station_id = sf.station_id
                JOIN train_stations tst
                    ON t.t_id = tst.t_id
                JOIN stations st
                    ON tst.station_id = st.station_id
                WHERE LOWER(sf.station_name) = LOWER(?)
                  AND LOWER(st.station_name) = LOWER(?)
                  AND tsf.sequence_no < tst.sequence_no
                ORDER BY tsf.departure_t
                """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {

            statement.setString(1, from);
            statement.setString(2, to);

            try (ResultSet result = statement.executeQuery()) {

                StringBuilder json = new StringBuilder();

                json.append("{\"success\":true,\"trains\":[");

                boolean first = true;

                while (result.next()) {

                    if (!first) {
                        json.append(",");
                    }

                    first = false;

                    json.append("{");

                    json.append("\"id\":")
                        .append(result.getInt("t_id"))
                        .append(",");

                    json.append("\"name\":\"")
                        .append(escapeJson(result.getString("t_name")))
                        .append("\",");

                    json.append("\"number\":\"")
                        .append(escapeJson(result.getString("t_no")))
                        .append("\",");

                    json.append("\"type\":\"")
                        .append(escapeJson(result.getString("t_type")))
                        .append("\",");

                    json.append("\"from\":\"")
                        .append(escapeJson(result.getString("from_station")))
                        .append("\",");

                    json.append("\"to\":\"")
                        .append(escapeJson(result.getString("to_station")))
                        .append("\",");

                    json.append("\"departure\":\"")
                        .append(result.getTime("departure_time"))
                        .append("\",");

                    json.append("\"arrival\":\"")
                        .append(result.getTime("arrival_time"))
                        .append("\"");

                    json.append("}");
                }

                json.append("]}");

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(json.toString());
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to search trains.\"}"
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