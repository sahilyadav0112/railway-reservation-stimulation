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

@WebServlet("/my-bookings")
public class MyBookingsServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String userIdParam =
                request.getParameter("userId");

        if (userIdParam == null ||
            userIdParam.isBlank()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"User ID is required.\"}"
            );

            return;
        }

        int userId;

        try {
            userId = Integer.parseInt(userIdParam);
        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Invalid user ID.\"}"
            );

            return;
        }


        String sql = """
                SELECT
                    b.b_id,
                    b.pnr,
                    b.user_id,
                    b.journey_date,
                    b.total_fare,
                    b.Booking_date,
                    b.booking_status,

                    t.t_name,
                    t.t_no,

                    tc.c_no,
                    tc.c_name,

                    (
                        SELECT s1.station_name
                        FROM train_stations ts1
                        JOIN stations s1
                            ON ts1.station_id = s1.station_id
                        WHERE ts1.t_id = b.train_id
                        ORDER BY ts1.sequence_no
                        LIMIT 1
                    ) AS from_station,

                    (
                        SELECT s2.station_name
                        FROM train_stations ts2
                        JOIN stations s2
                            ON ts2.station_id = s2.station_id
                        WHERE ts2.t_id = b.train_id
                        ORDER BY ts2.sequence_no DESC
                        LIMIT 1
                    ) AS to_station

                FROM booking b

                JOIN trains t
                    ON b.train_id = t.t_id

                JOIN train_classes tc
                    ON b.class_id = tc.c_id

                WHERE b.user_id = ?

                ORDER BY b.Booking_date DESC
                """;


        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);


            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                StringBuilder json =
                        new StringBuilder();

                json.append(
                        "{\"success\":true,\"bookings\":["
                );

                boolean first = true;


                while (result.next()) {

                    if (!first) {
                        json.append(",");
                    }

                    first = false;


                    json.append("{");

                    json.append(
                            "\"bookingId\":"
                    ).append(
                            result.getInt("b_id")
                    );


                    json.append(
                            ",\"pnr\":\""
                    ).append(
                            escapeJson(
                                    result.getString("pnr")
                            )
                    ).append("\"");


                    json.append(
                            ",\"userId\":"
                    ).append(
                            result.getInt("user_id")
                    );


                    json.append(
                            ",\"trainName\":\""
                    ).append(
                            escapeJson(
                                    result.getString("t_name")
                            )
                    ).append("\"");


                    json.append(
                            ",\"trainNumber\":\""
                    ).append(
                            escapeJson(
                                    result.getString("t_no")
                            )
                    ).append("\"");


                    json.append(
                            ",\"from\":\""
                    ).append(
                            escapeJson(
                                    result.getString("from_station")
                            )
                    ).append("\"");


                    json.append(
                            ",\"to\":\""
                    ).append(
                            escapeJson(
                                    result.getString("to_station")
                            )
                    ).append("\"");


                    json.append(
                            ",\"journeyDate\":\""
                    ).append(
                            result.getDate("journey_date")
                    ).append("\"");


                    json.append(
                            ",\"classCode\":\""
                    ).append(
                            escapeJson(
                                    result.getString("c_no")
                            )
                    ).append("\"");


                    json.append(
                            ",\"className\":\""
                    ).append(
                            escapeJson(
                                    result.getString("c_name")
                            )
                    ).append("\"");


                    json.append(
                            ",\"totalFare\":"
                    ).append(
                            result.getBigDecimal("total_fare")
                    );


                    json.append(
                            ",\"bookingDate\":\""
                    ).append(
                            escapeJson(
                                    String.valueOf(
                                            result.getTimestamp(
                                                    "Booking_date"
                                            )
                                    )
                            )
                    ).append("\"");


                    json.append(
                            ",\"status\":\""
                    ).append(
                            escapeJson(
                                    result.getString(
                                            "booking_status"
                                    )
                            )
                    ).append("\"");


                    json.append("}");
                }


                json.append("]}");


                response.setStatus(
                        HttpServletResponse.SC_OK
                );

                response.getWriter().write(
                        json.toString()
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Unable to load bookings.\"}"
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