package com.railway;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String bookingIdParam =
                request.getParameter("bookingId");

        String userIdParam =
                request.getParameter("userId");

        String paymentMethod =
                request.getParameter("paymentMethod");


        if (bookingIdParam == null ||
            userIdParam == null ||
            paymentMethod == null ||
            paymentMethod.isBlank()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Booking ID, user ID and payment method are required.\"}"
            );

            return;
        }


        int bookingId;
        int userId;


        try {

            bookingId =
                    Integer.parseInt(bookingIdParam);

            userId =
                    Integer.parseInt(userIdParam);

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Invalid booking or user ID.\"}"
            );

            return;
        }


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);


            /*
             * Verify that the booking belongs
             * to the logged-in user.
             */
            String bookingSql = """
                    SELECT
                        total_fare,
                        booking_status
                    FROM booking
                    WHERE b_id = ?
                      AND user_id = ?
                    FOR UPDATE
                    """;


            BigDecimal amount;
            String bookingStatus;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                bookingSql
                        )
            ) {

                statement.setInt(
                        1,
                        bookingId
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


                    amount =
                            result.getBigDecimal(
                                    "total_fare"
                            );

                    bookingStatus =
                            result.getString(
                                    "booking_status"
                            );
                }
            }


            if (!"CONFIRMED".equalsIgnoreCase(
                    bookingStatus
            )) {

                response.setStatus(
                        HttpServletResponse.SC_CONFLICT
                );

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Payment is allowed only for confirmed bookings.\"}"
                );

                return;
            }


            /*
             * Check whether payment already exists.
             */
            String existingPaymentSql = """
                    SELECT
                        payment_id,
                        payment_status
                    FROM payments
                    WHERE b_id = ?
                    ORDER BY payment_id DESC
                    LIMIT 1
                    """;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                existingPaymentSql
                        )
            ) {

                statement.setInt(
                        1,
                        bookingId
                );


                try (
                    ResultSet result =
                            statement.executeQuery()
                ) {

                    if (result.next()) {

                        String existingStatus =
                                result.getString(
                                        "payment_status"
                                );


                        if ("SUCCESS".equalsIgnoreCase(
                                existingStatus
                        )) {

                            connection.rollback();

                            response.setStatus(
                                    HttpServletResponse.SC_CONFLICT
                            );

                            response.getWriter().write(
                                    "{\"success\":false,\"message\":\"Payment has already been completed for this booking.\"}"
                            );

                            return;
                        }
                    }
                }
            }


            /*
             * Generate payment ID because the
             * team's payment_id is not AUTO_INCREMENT.
             */
            int paymentId;


            String maxPaymentSql = """
                    SELECT COALESCE(MAX(payment_id), 0) + 1
                    AS next_id
                    FROM payments
                    FOR UPDATE
                    """;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                maxPaymentSql
                        );

                ResultSet result =
                        statement.executeQuery()
            ) {

                if (!result.next()) {

                    throw new Exception(
                            "Unable to generate payment ID."
                    );
                }


                paymentId =
                        result.getInt("next_id");
            }


            /*
             * Insert simulated successful payment.
             */
            String paymentSql = """
                    INSERT INTO payments
                    (
                        payment_id,
                        b_id,
                        amount,
                        payment_method,
                        payment_status,
                        payment_date
                    )
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                paymentSql
                        )
            ) {

                statement.setInt(
                        1,
                        paymentId
                );

                statement.setInt(
                        2,
                        bookingId
                );

                statement.setBigDecimal(
                        3,
                        amount
                );

                statement.setString(
                        4,
                        paymentMethod
                );

                statement.setString(
                        5,
                        "SUCCESS"
                );

                statement.setObject(
                        6,
                        LocalDateTime.now()
                );

                statement.executeUpdate();
            }


            connection.commit();


            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );


            response.getWriter().write(
                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Payment successful.\","
                    + "\"paymentId\":"
                    + paymentId
                    + ","
                    + "\"bookingId\":"
                    + bookingId
                    + ","
                    + "\"amount\":"
                    + amount
                    + ","
                    + "\"paymentMethod\":\""
                    + escapeJson(paymentMethod)
                    + "\","
                    + "\"paymentStatus\":\"SUCCESS\""
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
                    "{"
                    + "\"success\":false,"
                    + "\"message\":\"Payment failed.\""
                    + "}"
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