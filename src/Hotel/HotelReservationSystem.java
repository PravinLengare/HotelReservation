package Hotel;

import sun.dc.pr.PRError;

import java.sql.*;
import java.util.Scanner;

import static javafx.application.Platform.exit;

public class HotelReservationSystem {
    private static final String url = "jdbc:postgresql://localhost:5432/hotel_db";
    private static final String username = "postgres";
    private static final String password = "Pravin@123";



    public static void main(String[] args) throws SQLException ,ClassNotFoundException {
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (ClassNotFoundException e){
            System.out.println(e.getMessage());
        }

        try {
            Connection connection = DriverManager.getConnection(url,username,password);
            Statement statement = connection.createStatement();
            while(true){
                System.out.println();
                System.out.println("HOTEL MANAGEMENT SYSTEM");
                Scanner scanner = new Scanner(System.in);
                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservation");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reservation");
                System.out.println("5. Delete Reservation");
                System.out.println("0. Exit");
                System.out.println("Choose an Option : ");
                int choice = scanner.nextInt();
                switch (choice){
                    case 1:
                        reservation(connection ,scanner);
                        break;

                    case 2:
                        viewReservation(connection);
                        break;

                    case 3:
                        getRoomNumber(connection,scanner);
                        break;

                    case 4:
                        updateReservation(connection,scanner);
                        break;

                    case 5:
                        deleteReservation(connection,scanner);
                        break;

                    case 0:
                        exit();
                        scanner.close();
                        return;

                    default:
                        System.out.println("Invalid choice please try again !");

                }
            }

        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }


    private static void reservation(Connection connection,Scanner scanner){
        try {
            System.out.println("Enter guest name : ");
            scanner.nextLine();
            String guest_name = scanner.nextLine();
            System.out.println("Enter room number : ");
            int room_no = scanner.nextInt();
            scanner.nextLine();
            System.out.println("Enter contact number : ");
            String contact_no = scanner.nextLine();


            String query = "INSERT INTO reservations (guest_name,room_number,contact_number)" +  "VALUES ('"+ guest_name +"','"+ room_no +"','"+ contact_no +"')";

            try(Statement statement = connection.createStatement()){

                int affectedRows = statement.executeUpdate(query);
                if (affectedRows > 0) {
                    System.out.println("Reservation Successful !!");
                }
                else {
                    System.out.println("Reservation Failed !!");
                }
            }


        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    private static void viewReservation(Connection connection) throws SQLException {
        String query = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date FROM reservations";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            System.out.println("Current Reservations:");
            System.out.println("+----------------+-----------------+--------------+--------------------+----------------------------+");
            System.out.println("| Reservation ID | Guest Name      | Room Number  | Contact Number     | Reservation Date           |");
            System.out.println("+----------------+-----------------+--------------+--------------------+----------------------------+");

            while (resultSet.next()) {
                int reservationId = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                String roomNumber = resultSet.getString("room_number"); // VARCHAR in DB, so String
                String contactNumber = resultSet.getString("contact_number");
                String reservationDate = resultSet.getTimestamp("reservation_date").toString();

                System.out.printf("| %-14d | %-15s | %-12s | %-18s | %-19s |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate);
            }

            System.out.println("+----------------+-----------------+--------------+--------------------+----------------------------+");
        }
    }

    private static void getRoomNumber(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter the reservation ID: ");
            int reservationID = scanner.nextInt();
            scanner.nextLine(); // Consume leftover newline

            System.out.print("Enter guest name: ");
            String guestName = scanner.nextLine();

            String query = "SELECT room_number FROM reservations WHERE reservation_id = ? AND guest_name = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)){
                statement.setInt(1, reservationID);
                statement.setString(2, guestName);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String roomNumber = resultSet.getString("room_number");
                        System.out.println("Room number for Reservation ID " + reservationID +
                                " and Guest '" + guestName + "' is: " + roomNumber);
                    } else {
                        System.out.println("No reservation found for the given details.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateReservation(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter the Reservation ID to Update: ");
            int reservationID = scanner.nextInt();
            scanner.nextLine(); // consume newline

            if (!reservationExists(connection, reservationID)) {
                System.out.println("Reservation not found for the given ID: " + reservationID);
                return;
            }

            System.out.print("Enter new guest name: ");
            String newGuestName = scanner.nextLine();

            System.out.print("Enter new room number: ");
            String newRoomNo = scanner.nextLine(); // room_number is VARCHAR(10)

            System.out.print("Enter new contact number: ");
            String newContactNo = scanner.nextLine();

            String query = "UPDATE reservations SET guest_name = ?, room_number = ?, contact_number = ? WHERE reservation_id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, newGuestName);
                preparedStatement.setString(2, newRoomNo);
                preparedStatement.setString(3, newContactNo);
                preparedStatement.setInt(4, reservationID);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Reservation Updated Successfully!");
                } else {
                    System.out.println("Update Failed!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteReservation(Connection connection,Scanner scanner){
        try{
            System.out.println("Enter the Reservation ID : ");
            int reservationID = scanner.nextInt();

            if (!reservationExists(connection,reservationID) ){
                System.out.println("Reservation not found for the given ID :");
                return;
            }

            String query = "DELETE FROM reservations WHERE reservation_id = "+reservationID;

            try(Statement statement = connection.createStatement()) {
                int rowsAffected = statement.executeUpdate(query);

                if (rowsAffected > 0){

                    System.out.println("Reservation deleted  Successfully !!");
                }
                else {
                    System.out.println("Delete Reservation Failed!");
                }
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    private static boolean reservationExists(Connection connection,int reservationID){
        try {
            String query = "SELECT reservation_id FROM reservations  WHERE reservation_id = "+reservationID;

            try(Statement statement = connection.createStatement();
               ResultSet resultSet = statement.executeQuery(query)) {

                return resultSet.next();

            }
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public static void exit() throws InterruptedException{
        System.out.print("Exiting System");
        int i = 5;
        while (i != 0){
            System.out.print(".");
            Thread.sleep(450);
            i--;
        }
        System.out.println();
        System.out.println("ThankYou For Using Hotel Management System !!");
    }
}
