import java.sql.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class HotelReservationSystem {

    // mysql database url
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_db";

    // mysql user credentials
    private static final String USER = "root";
    private static final String PASSWORD = "Loki0987@";

    // to store room numbers for allocating purposes
    private static final Set<Integer> hs = new HashSet<>();
    private static final Random random = new Random();

    // main method
    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        // loading the jdbc driver
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        // connecting jdbc driver to mysql database
        try{
            Connection connection = DriverManager.getConnection(URL,USER,PASSWORD);
            Statement statement = connection.createStatement();
            // Homepage Interface
            while(true){
                System.out.println();
                System.out.println("HOTEL RESERVATION SYSTEM");
                Scanner sc = new Scanner(System.in);
                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservations");
                System.out.println("3. Get a Room Number");
                System.out.println("4. Update Reservation");
                System.out.println("5. Delete Reservation");
                System.out.println("0. Exit");
                System.out.print("Choose an Option: ");
                int choice = sc.nextInt();
                switch(choice){
                    case 1:
                        reserveRoom(statement, sc);
                        break;
                    case 2:
                        viewReservations(statement);
                        break;
                    case 3:
                        getRoomNumber(statement, sc);
                        break;
                    case 4:
                        updateReservation(statement, sc);
                        break;
                    case 5:
                        deleteReservation(statement, sc);
                        break;
                    case 0:
                        exit();
                        sc.close();
                        return ;
                    default:
                        System.out.println("Invalid Option, Try Again!!!");
                }
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    // reserving a Room
    private static void reserveRoom(Statement statement, Scanner sc){
        try{
            int roomNumber = allotRoomNumber();
            System.out.println("Enter Guest Name: ");
            String guestName = sc.next();
            sc.nextLine();
            System.out.println("Enter Guest's Contact Number: ");
            String contactNumber = sc.next();
            sc.nextLine();

            // creating sql query
            String sql_query = "INSERT INTO reservations(guest_name,room_number,contact_number)" +
                    "VALUES('" + guestName + "','" + roomNumber + "','" + contactNumber + "');";

            int affectedRows = statement.executeUpdate(sql_query);
            if (affectedRows > 0){
                System.out.println("Reservation Successful!!");
            }else {
                System.out.println("Reservation Failed!!");
            }

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private static int allotRoomNumber(){
        try {
            if (hs.size() > 100) {
                throw new Exception("Rooms are Full!! Sorry For Inconvenience!!");
            }
            int randomNumber;
            do {
                randomNumber = random.nextInt(100) + 100;
            } while (hs.contains(randomNumber));
            hs.add(randomNumber);
            return randomNumber;
        } catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    // retrieving current reservations
    private static void viewReservations(Statement statement) throws SQLException{

        // sql query
        String sql_query = "SELECT reservation_id,guest_name,room_number,contact_number,reservation_date FROM reservations";
        try{
            ResultSet resultSet = statement.executeQuery(sql_query);

            System.out.println("Current Reservations: ");
            System.out.println("+----------------+-----------------+---------------+----------------------+---------------------+");
            System.out.println("| Reservation ID | Guest           | Room Number   | Contact Number       | Reservation Date    |");
            System.out.println("+----------------+-----------------+---------------+----------------------+---------------------+");
            while(resultSet.next()){
                int reservationId = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_number");
                String contactNumber = resultSet.getString("contact_number");
                String reservationDate = resultSet.getString("reservation_date");

                // Displaying the reservations data in table format
                System.out.printf("| %-14d | %-15s | %-13d | %-20s | %-19s |\n",
                        reservationId,guestName,roomNumber,contactNumber,reservationDate);
            }
            System.out.println("+----------------+-----------------+---------------+----------------------+---------------------+");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    // getting the room number
    private static void getRoomNumber(Statement statement, Scanner sc) throws SQLException {
        try {
            System.out.println("Enter reservation ID: ");
            int reservationId = sc.nextInt();
            sc.nextLine();
            System.out.println("Enter guest Name: ");
            String guestName = sc.next();
            sc.nextLine();

            // sql_query
            String sql_query = "SELECT room_number FROM reservations WHERE reservation_id = " + reservationId + " and guest_name = '" + guestName + "';";
            ResultSet resultSet = statement.executeQuery(sql_query);

            if (resultSet.next()) {
                int roomNumber = resultSet.getInt("room_number");
                System.out.println("Room Number for Reservation ID " + reservationId + " and guest " + guestName + " is " + roomNumber);
            } else {
                System.out.println("Room Number Not Found for Given Reservation ID and Guest!!");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void updateReservation(Statement statement, Scanner sc) throws SQLException{
            try{
                System.out.println("Enter reservation ID to update : ");
                int reservationId = sc.nextInt();
                sc.nextLine();

                if (!reservationExists(statement, reservationId)) {
                    System.out.println("Reservation not found for the given ID.");
                    return;
                }

                System.out.print("Enter new guest name: ");
                String newGuestName = sc.nextLine();
                System.out.print("Enter new contact number: ");
                String newContactNumber = sc.next();

                String sql_query = "UPDATE reservations SET guest_name = '" + newGuestName + "', " +
                        "contact_number = '" + newContactNumber + "' " +
                        "WHERE reservation_id = " + reservationId;

                int affectedRows = statement.executeUpdate(sql_query);
                if (affectedRows > 0) {
                    System.out.println("Reservation updated successfully!");
                } else {
                    System.out.println("Reservation update failed.");
                }
            } catch (SQLException e){
                System.out.println(e.getMessage());
            }
        }

    // deleting the reservation
    private static void deleteReservation(Statement statement, Scanner sc) throws SQLException{
        try {
            System.out.print("Enter reservation ID to delete: ");
            int reservationId = sc.nextInt();

            if (!reservationExists(statement, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            String sql_query = "DELETE FROM reservations WHERE reservation_id = " + reservationId;

            int affectedRows = statement.executeUpdate(sql_query);
            if (affectedRows > 0) {
                System.out.println("Reservation deleted successfully!");
            } else {
                System.out.println("Reservation deletion failed.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // finding reservation id exists or not
    private static boolean reservationExists(Statement statement, int reservationId) {
        try {
            String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = " + reservationId;
            ResultSet resultSet = statement.executeQuery(sql);
            return resultSet.next(); // If there's a result, the reservation exists
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false; // Handle database errors as needed
        }
    }

    // exit
    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i = 5;
        while(i!=0){
            System.out.print(".");
            Thread.sleep(1000);
            i--;
        }
        System.out.println();
        System.out.println("ThankYou For Using Hotel Reservation System!!!");
    }

}
