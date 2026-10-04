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

@WebServlet("/cancel-booking")
public class CancelBookingServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pnr =
                request.getParameter("pnr");

        String userIdParam =
                request.getParameter("userId");


        if (pnr == null ||
            pnr.isBlank() ||
            userIdParam == null ||
            userIdParam.isBlank()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"PNR and user ID are required.\"}"
            );

            return;
        }


        int userId;

        try {

            userId =
                    Integer.parseInt(userIdParam);

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Invalid user ID.\"}"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);


            /*
             * Find the booking and verify
             * that it belongs to the logged-in user.
             */
            String findBookingSql = """
                    SELECT
                        b_id,
                        booking_status
                    FROM booking
                    WHERE pnr = ?
                      AND user_id = ?
                    FOR UPDATE
                    """;


            int bookingId;
            String bookingStatus;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                findBookingSql
                        )
            ) {

                statement.setString(
                        1,
                        pnr
                );

                statement.setInt(
                        2,
                        userId
                );


                try (
                    ResultSet result =
                            statement.executeQuery()
                ) {

                    if (!result.next()) {

                        response.setStatus(
                                HttpServletResponse.SC_NOT_FOUND
                        );

                        response.getWriter().write(
                                "{\"success\":false,\"message\":\"Booking not found.\"}"
                        );

                        return;
                    }


                    bookingId =
                            result.getInt(
                                    "b_id"
                            );

                    bookingStatus =
                            result.getString(
                                    "booking_status"
                            );
                }
            }


            /*
             * Do not cancel an already
             * cancelled booking.
             */
            if ("CANCELLED".equalsIgnoreCase(
                    bookingStatus
            )) {

                response.setStatus(
                        HttpServletResponse.SC_CONFLICT
                );

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Booking is already cancelled.\"}"
                );

                return;
            }


            /*
             * Get the seats belonging to
             * this booking.
             */
            String seatSql = """
                    SELECT seat_id
                    FROM booking_passengers
                    WHERE booking_id = ?
                      AND seat_id IS NOT NULL
                    """;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                seatSql
                        )
            ) {

                statement.setInt(
                        1,
                        bookingId
                );


                /*
                 * Release every seat.
                 */
                String updateSeatSql = """
                        UPDATE seat
                        SET status = 'AVAILABLE'
                        WHERE seat_id = ?
                        """;


                try (
                    ResultSet result =
                            statement.executeQuery();

                    PreparedStatement seatStatement =
                            connection.prepareStatement(
                                    updateSeatSql
                            )
                ) {

                    while (result.next()) {

                        int seatId =
                                result.getInt(
                                        "seat_id"
                                );

                        seatStatement.setInt(
                                1,
                                seatId
                        );

                        seatStatement.executeUpdate();
                    }
                }
            }


            /*
             * Mark passengers as cancelled.
             */
            String updatePassengerSql = """
                    UPDATE booking_passengers
                    SET status = 'CANCELLED'
                    WHERE booking_id = ?
                    """;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                updatePassengerSql
                        )
            ) {

                statement.setInt(
                        1,
                        bookingId
                );

                statement.executeUpdate();
            }


            /*
             * Mark the booking itself as cancelled.
             */
            String updateBookingSql = """
                    UPDATE booking
                    SET booking_status = 'CANCELLED'
                    WHERE b_id = ?
                    """;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                updateBookingSql
                        )
            ) {

                statement.setInt(
                        1,
                        bookingId
                );

                statement.executeUpdate();
            }


            connection.commit();


            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            response.getWriter().write(
                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Booking cancelled successfully.\","
                    + "\"pnr\":\""
                    + escapeJson(pnr)
                    + "\""
                    + "}"
            );


        } catch (Exception e) {

            e.printStackTrace();


            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (Exception rollbackError) {

                rollbackError.printStackTrace();
            }


            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Unable to cancel booking.\"}"
            );


        } finally {

            try {

                if (connection != null) {

                    connection.setAutoCommit(true);
                    connection.close();
                }

            } catch (Exception closeError) {

                closeError.printStackTrace();
            }
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