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
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDateTime;

@WebServlet("/book")
public class BookingServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String userIdParam = request.getParameter("userId");
        String trainIdParam = request.getParameter("trainId");
        String classIdParam = request.getParameter("classId");
        String coachIdParam = request.getParameter("coachId");
        String journeyDateParam = request.getParameter("journeyDate");

        String[] names =
                request.getParameterValues("passengerName");

        String[] ages =
                request.getParameterValues("passengerAge");

        String[] genders =
                request.getParameterValues("passengerGender");


        // Validate required data
        if (userIdParam == null ||
            trainIdParam == null ||
            classIdParam == null ||
            coachIdParam == null ||
            journeyDateParam == null ||
            names == null ||
            ages == null ||
            genders == null) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Required booking information is missing.\"}"
            );

            return;
        }


        if (names.length == 0 ||
            names.length != ages.length ||
            names.length != genders.length) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Invalid passenger information.\"}"
            );

            return;
        }


        Connection connection = null;

        try {

            int userId = Integer.parseInt(userIdParam);
            int trainId = Integer.parseInt(trainIdParam);
            int classId = Integer.parseInt(classIdParam);
            int coachId = Integer.parseInt(coachIdParam);

            Date journeyDate =
                    Date.valueOf(journeyDateParam);


            connection =
                    DBConnection.getConnection();

            /*
             * Start transaction.
             */
            connection.setAutoCommit(false);


            /*
             * 1. Get the next booking ID.
             */
            int bookingId;

            String maxBookingSql = """
                    SELECT COALESCE(MAX(b_id), 0) + 1 AS next_id
                    FROM booking
                    FOR UPDATE
                    """;

            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                maxBookingSql
                        );

                ResultSet result =
                        statement.executeQuery()
            ) {

                if (!result.next()) {
                    throw new Exception(
                            "Unable to generate booking ID."
                    );
                }

                bookingId =
                        result.getInt("next_id");
            }


            /*
             * 2. Find available seats in
             *    the selected coach.
             */
            String seatSql = """
                    SELECT seat_id
                    FROM seat
                    WHERE coach_id = ?
                      AND status = 'AVAILABLE'
                    ORDER BY seat_id
                    LIMIT ?
                    FOR UPDATE
                    """;


            int passengerCount =
                    names.length;

            int[] seatIds =
                    new int[passengerCount];


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                seatSql
                        )
            ) {

                statement.setInt(
                        1,
                        coachId
                );

                statement.setInt(
                        2,
                        passengerCount
                );


                try (
                    ResultSet result =
                            statement.executeQuery()
                ) {

                    int index = 0;

                    while (result.next()) {

                        seatIds[index] =
                                result.getInt("seat_id");

                        index++;
                    }


                    if (index < passengerCount) {

                        connection.rollback();

                        response.setStatus(
                                HttpServletResponse.SC_CONFLICT
                        );

                        response.getWriter().write(
                                "{\"success\":false,\"message\":\"Not enough seats available.\"}"
                        );

                        return;
                    }
                }
            }


            /*
             * 3. Get fare from the team's
             *    train_classes table.
             */
            BigDecimal farePerPassenger;

            String fareSql = """
                    SELECT base_fare
                    FROM train_classes
                    WHERE c_id = ?
                    """;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                fareSql
                        )
            ) {

                statement.setInt(
                        1,
                        classId
                );


                try (
                    ResultSet result =
                            statement.executeQuery()
                ) {

                    if (!result.next()) {

                        connection.rollback();

                        response.setStatus(
                                HttpServletResponse.SC_BAD_REQUEST
                        );

                        response.getWriter().write(
                                "{\"success\":false,\"message\":\"Invalid train class.\"}"
                        );

                        return;
                    }

                    farePerPassenger =
                            result.getBigDecimal(
                                    "base_fare"
                            );
                }
            }


            BigDecimal totalFare =
                    farePerPassenger.multiply(
                            BigDecimal.valueOf(
                                    passengerCount
                            )
                    );


            /*
             * 4. Generate PNR.
             */
            String pnr =
                    "RB" +
                    System.currentTimeMillis();


            /*
             * 5. Insert booking.
             */
            String bookingSql = """
                    INSERT INTO booking
                    (
                        b_id,
                        pnr,
                        user_id,
                        train_id,
                        journey_date,
                        class_id,
                        total_fare,
                        Booking_date,
                        booking_status
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;


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

                statement.setString(
                        2,
                        pnr
                );

                statement.setInt(
                        3,
                        userId
                );

                statement.setInt(
                        4,
                        trainId
                );

                statement.setDate(
                        5,
                        journeyDate
                );

                statement.setInt(
                        6,
                        classId
                );

                statement.setBigDecimal(
                        7,
                        totalFare
                );

                statement.setObject(
                        8,
                        LocalDateTime.now()
                );

                statement.setString(
                        9,
                        "CONFIRMED"
                );

                statement.executeUpdate();
            }


            /*
             * 6. Get the next passenger ID.
             */
            int nextPassengerId;

            String maxPassengerSql = """
                    SELECT COALESCE(MAX(p_id), 0) + 1 AS next_id
                    FROM passenger
                    FOR UPDATE
                    """;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                maxPassengerSql
                        );

                ResultSet result =
                        statement.executeQuery()
            ) {

                if (!result.next()) {

                    throw new Exception(
                            "Unable to generate passenger ID."
                    );
                }

                nextPassengerId =
                        result.getInt("next_id");
            }


            /*
             * 7. Insert passengers.
             */
            String passengerSql = """
                    INSERT INTO passenger
                    (
                        p_id,
                        user_id,
                        p_name,
                        p_age,
                        p_gender
                    )
                    VALUES (?, ?, ?, ?, ?)
                    """;


            String bookingPassengerSql = """
                    INSERT INTO booking_passengers
                    (
                        booking_id,
                        p_id,
                        seat_id,
                        status
                    )
                    VALUES (?, ?, ?, ?)
                    """;


            try (
                PreparedStatement passengerStatement =
                        connection.prepareStatement(
                                passengerSql
                        );

                PreparedStatement bookingPassengerStatement =
                        connection.prepareStatement(
                                bookingPassengerSql
                        )
            ) {

                for (int i = 0;
                     i < passengerCount;
                     i++) {

                    int passengerId =
                            nextPassengerId + i;


                    passengerStatement.setInt(
                            1,
                            passengerId
                    );

                    passengerStatement.setInt(
                            2,
                            userId
                    );

                    passengerStatement.setString(
                            3,
                            names[i]
                    );

                    passengerStatement.setInt(
                            4,
                            Integer.parseInt(
                                    ages[i]
                            )
                    );

                    passengerStatement.setString(
                            5,
                            genders[i]
                    );

                    passengerStatement.executeUpdate();


                    bookingPassengerStatement.setInt(
                            1,
                            bookingId
                    );

                    bookingPassengerStatement.setInt(
                            2,
                            passengerId
                    );

                    bookingPassengerStatement.setInt(
                            3,
                            seatIds[i]
                    );

                    bookingPassengerStatement.setString(
                            4,
                            "CONFIRMED"
                    );

                    bookingPassengerStatement.executeUpdate();
                }
            }


            /*
             * 8. Mark selected seats as BOOKED.
             */
            String updateSeatSql = """
                    UPDATE seat
                    SET status = 'BOOKED'
                    WHERE seat_id = ?
                    """;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                updateSeatSql
                        )
            ) {

                for (int seatId : seatIds) {

                    statement.setInt(
                            1,
                            seatId
                    );

                    statement.executeUpdate();
                }
            }


            /*
             * 9. Commit the complete booking.
             */
            connection.commit();


            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            response.getWriter().write(
                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Booking successful.\","
                    + "\"bookingId\":" + bookingId + ","
                    + "\"pnr\":\"" + pnr + "\","
                    + "\"totalFare\":" + totalFare
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
                    + "\"message\":\"Booking failed.\""
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
}