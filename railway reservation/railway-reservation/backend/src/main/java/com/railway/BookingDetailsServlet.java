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

@WebServlet("/booking")
public class BookingDetailsServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pnr = request.getParameter("pnr");

        if (pnr == null || pnr.isBlank()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"PNR is required.\"}"
            );

            return;
        }


        String bookingSql = """
                SELECT
                    b.b_id,
                    b.pnr,
                    b.user_id,
                    b.train_id,
                    b.journey_date,
                    b.class_id,
                    b.total_fare,
                    b.Booking_date,
                    b.booking_status,

                    t.t_name,
                    t.t_no,

                    tc.c_no,
                    tc.c_name

                FROM booking b

                JOIN trains t
                    ON b.train_id = t.t_id

                JOIN train_classes tc
                    ON b.class_id = tc.c_id

                WHERE b.pnr = ?
                """;


        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement bookingStatement =
                    connection.prepareStatement(
                            bookingSql
                    )
        ) {

            bookingStatement.setString(
                    1,
                    pnr
            );


            try (
                ResultSet bookingResult =
                        bookingStatement.executeQuery()
            ) {

                if (!bookingResult.next()) {

                    response.setStatus(
                            HttpServletResponse.SC_NOT_FOUND
                    );

                    response.getWriter().write(
                            "{\"success\":false,\"message\":\"Ticket not found.\"}"
                    );

                    return;
                }


                int bookingId =
                        bookingResult.getInt(
                                "b_id"
                        );

                int userId =
                        bookingResult.getInt(
                                "user_id"
                        );

                String bookingPnr =
                        bookingResult.getString(
                                "pnr"
                        );

                int trainId =
                        bookingResult.getInt(
                                "train_id"
                        );

                String trainName =
                        bookingResult.getString(
                                "t_name"
                        );

                String trainNumber =
                        bookingResult.getString(
                                "t_no"
                        );

                String journeyDate =
                        String.valueOf(
                                bookingResult.getDate(
                                        "journey_date"
                                )
                        );

                String classCode =
                        bookingResult.getString(
                                "c_no"
                        );

                String className =
                        bookingResult.getString(
                                "c_name"
                        );

                String totalFare =
                        String.valueOf(
                                bookingResult.getBigDecimal(
                                        "total_fare"
                                )
                        );

                String bookingDate =
                        String.valueOf(
                                bookingResult.getTimestamp(
                                        "Booking_date"
                                )
                        );

                String bookingStatus =
                        bookingResult.getString(
                                "booking_status"
                        );


                /*
                 * Get the first and last station
                 * of this train.
                 */
                String routeSql = """
                        SELECT
                            sf.station_name AS from_station,
                            st.station_name AS to_station,
                            sf_ts.departure_t,
                            st_ts.arrival_t

                        FROM train_stations sf_ts

                        JOIN stations sf
                            ON sf_ts.station_id =
                               sf.station_id

                        JOIN train_stations st_ts
                            ON st_ts.t_id =
                               sf_ts.t_id

                        JOIN stations st
                            ON st_ts.station_id =
                               st.station_id

                        WHERE sf_ts.t_id = ?

                        AND sf_ts.sequence_no =
                            (
                                SELECT MIN(sequence_no)
                                FROM train_stations
                                WHERE t_id = ?
                            )

                        AND st_ts.sequence_no =
                            (
                                SELECT MAX(sequence_no)
                                FROM train_stations
                                WHERE t_id = ?
                            )
                        """;


                String fromStation = "";
                String toStation = "";
                String departure = "";
                String arrival = "";


                try (
                    PreparedStatement routeStatement =
                            connection.prepareStatement(
                                    routeSql
                            )
                ) {

                    routeStatement.setInt(
                            1,
                            trainId
                    );

                    routeStatement.setInt(
                            2,
                            trainId
                    );

                    routeStatement.setInt(
                            3,
                            trainId
                    );


                    try (
                        ResultSet routeResult =
                                routeStatement.executeQuery()
                    ) {

                        if (routeResult.next()) {

                            fromStation =
                                    routeResult.getString(
                                            "from_station"
                                    );

                            toStation =
                                    routeResult.getString(
                                            "to_station"
                                    );

                            departure =
                                    String.valueOf(
                                            routeResult.getTime(
                                                    "departure_t"
                                            )
                                    );

                            arrival =
                                    String.valueOf(
                                            routeResult.getTime(
                                                    "arrival_t"
                                            )
                                    );
                        }
                    }
                }


                /*
                 * Get passengers and seats
                 */
                String passengerSql = """
                        SELECT
                            bp.p_id,
                            bp.seat_id,
                            bp.status,

                            p.p_name,
                            p.p_age,
                            p.p_gender,

                            s.seat_number

                        FROM booking_passengers bp

                        JOIN passenger p
                            ON bp.p_id = p.p_id

                        LEFT JOIN seat s
                            ON bp.seat_id = s.seat_id

                        WHERE bp.booking_id = ?

                        ORDER BY bp.p_id
                        """;


                StringBuilder passengers =
                        new StringBuilder();

                passengers.append("[");


                boolean firstPassenger = true;


                try (
                    PreparedStatement passengerStatement =
                            connection.prepareStatement(
                                    passengerSql
                            )
                ) {

                    passengerStatement.setInt(
                            1,
                            bookingId
                    );


                    try (
                        ResultSet passengerResult =
                                passengerStatement.executeQuery()
                    ) {

                        while (
                            passengerResult.next()
                        ) {

                            if (!firstPassenger) {
                                passengers.append(",");
                            }

                            firstPassenger = false;


                            passengers.append("{");


                            passengers.append(
                                    "\"id\":"
                            ).append(
                                    passengerResult.getInt(
                                            "p_id"
                                    )
                            );


                            passengers.append(
                                    ",\"name\":\""
                            ).append(
                                    escapeJson(
                                            passengerResult.getString(
                                                    "p_name"
                                            )
                                    )
                            ).append("\"");


                            passengers.append(
                                    ",\"age\":"
                            ).append(
                                    passengerResult.getInt(
                                            "p_age"
                                    )
                            );


                            passengers.append(
                                    ",\"gender\":\""
                            ).append(
                                    escapeJson(
                                            passengerResult.getString(
                                                    "p_gender"
                                            )
                                    )
                            ).append("\"");


                            passengers.append(
                                    ",\"seatId\":"
                            ).append(
                                    passengerResult.getInt(
                                            "seat_id"
                                    )
                            );


                            passengers.append(
                                    ",\"seatNumber\":\""
                            ).append(
                                    escapeJson(
                                            passengerResult.getString(
                                                    "seat_number"
                                            )
                                    )
                            ).append("\"");


                            passengers.append(
                                    ",\"status\":\""
                            ).append(
                                    escapeJson(
                                            passengerResult.getString(
                                                    "status"
                                            )
                                    )
                            ).append("\"");


                            passengers.append("}");
                        }
                    }
                }


                passengers.append("]");


                String json =
                        "{"
                        + "\"success\":true,"
                        + "\"bookingId\":" + bookingId + ","
                        + "\"userId\":" + userId + ","
                        + "\"pnr\":\""
                        + escapeJson(bookingPnr)
                        + "\","
                        + "\"trainName\":\""
                        + escapeJson(trainName)
                        + "\","
                        + "\"trainNumber\":\""
                        + escapeJson(trainNumber)
                        + "\","
                        + "\"journeyDate\":\""
                        + escapeJson(journeyDate)
                        + "\","
                        + "\"classCode\":\""
                        + escapeJson(classCode)
                        + "\","
                        + "\"className\":\""
                        + escapeJson(className)
                        + "\","
                        + "\"from\":\""
                        + escapeJson(fromStation)
                        + "\","
                        + "\"to\":\""
                        + escapeJson(toStation)
                        + "\","
                        + "\"departure\":\""
                        + escapeJson(departure)
                        + "\","
                        + "\"arrival\":\""
                        + escapeJson(arrival)
                        + "\","
                        + "\"totalFare\":"
                        + totalFare
                        + ","
                        + "\"bookingDate\":\""
                        + escapeJson(bookingDate)
                        + "\","
                        + "\"status\":\""
                        + escapeJson(bookingStatus)
                        + "\","
                        + "\"passengers\":"
                        + passengers
                        + "}";


                response.setStatus(
                        HttpServletResponse.SC_OK
                );

                response.getWriter().write(
                        json
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Unable to load ticket details.\"}"
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