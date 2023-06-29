/**
 * Name: Liezl Laurel
 * Date: April 18, 2023
 * Description: This program for CSC 3300-001 demonstrates CRUD operations on a University database using JDBC.
 * It allows the user to connect to the database with their MySQL username and password and perform the following operations:
 * retrieving all departments and/or courses, adding a new course, deleting a course, and updating a course.
 **/

// import all sql libraries and scanner class, which allows us to get user input
import java.sql.*;
import java.util.Scanner;

// class to demonstrate CRUD operations on our University database using JDBC
class UniversityDatabaseApp {
    private static final String URL = "jdbc:mysql://localhost:3306/university";
    private static String user;
    private static String password;

    public static void main(String[] args) throws SQLException {
        // create scanner object to read user input from the console
        Scanner scan = new Scanner(System.in);
        // connecting to the database using the provided credentials
        Connection conn = null;
        boolean authenticated = false;
        String cont = "";
        do {
            // Prompt the user to enter MySQL username and password
            System.out.print("Enter MySQL username: ");
            String username = scan.nextLine();
            System.out.print("Enter MySQL password: ");
            String password = scan.nextLine();

            try {
                conn = DriverManager.getConnection(URL, username, password);
                authenticated = true;
                System.out.println("Connected to the University database!");
                cont = ""; // successful connection, so exit do while loop
            } catch (SQLException e) {
                System.out.print("Invalid username or password, would you like to try again? (Y/N): ");
                cont = scan.nextLine();
                if (cont.equalsIgnoreCase("N")) {
                    System.out.print("Goodbye!\n");
                    System.exit(0);
                }
                while (!cont.equalsIgnoreCase("Y") && !cont.equalsIgnoreCase("N"))
                {
                    System.out.print("Invalid input. Choose (Y/N) ");
                    cont = scan.nextLine();
                }
            }
        } while (cont.equalsIgnoreCase("Y") && authenticated == false);

        // continuously prompt the user for input until the program is exited
        while (true) {
            // Print menu and prompt user for choice
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Retrieve all departments");
            System.out.println("2. Add a course");
            System.out.println("3. Retrieve all courses");
            System.out.println("4. Delete a course");
            System.out.println("5. Update a course");
            System.out.println("6. Exit");

            System.out.print("Enter your choice: ");
            int choice = Integer.parseInt(scan.nextLine());

            // calls respective function based on choice
            switch (choice) {
                case 1:
                    retrieveDepartments(conn);
                    break;
                case 2:
                    addCourse(conn, scan);
                    break;
                case 3:
                    retrieveCourses(conn);
                    break;
                case 4:
                    deleteCourse(conn, scan);
                    break;
                case 5:
                    updateCourse(conn, scan);
                    break;
                case 6:
                    System.out.println("Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid option, please try again: ");
            }
        }
    }

    // Retrieve department info (name, building) and print them to the console
    private static void retrieveDepartments(Connection conn) throws SQLException {
        // use prepared statement to retrieve dept info
        String sql = "SELECT dept_name, building FROM department";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nDepartments\n--------------------");
                while (rs.next()) {
                    String deptName = rs.getString("dept_name");
                    String building = rs.getString("building");
                    System.out.println("dept_name: " + deptName + "\nbuilding: " + building + "\n");
                }
            }
        }
    }

    // Add a new course to the database
    private static void addCourse(Connection conn, Scanner scan) throws SQLException {
        try {
            System.out.print("Enter course ID: ");
            String courseId = scan.nextLine();

            System.out.print("Enter course title: ");
            String courseTitle = scan.nextLine();

            boolean validDeptName = false;
            String deptName = null;
            while (!validDeptName) {
                System.out.print("Enter department name: ");
                deptName = scan.nextLine();
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM department WHERE dept_name=?")) {
                    pstmt.setString(1, deptName);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        validDeptName = true;
                    } else {
                        System.out.println("Invalid department name, please enter a valid department name.");
                    }
                }
            }

            int credits = 0;
            boolean validCredits = false;
            while (!validCredits) {
                System.out.print("Enter number of credits: ");
                if (scan.hasNextInt()) {
                    credits = scan.nextInt();
                    scan.nextLine(); // consume newline character
                    if (credits > 0) {
                        validCredits = true;
                    } else {
                        System.out.println("Invalid number of credits, please enter a positive integer.");
                    }
                } else {
                    System.out.println("Invalid input, please enter an integer.");
                    scan.nextLine(); // consume invalid input
                }
            }
            // use prepared statement to add course based on user input
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO course VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, courseId);
                pstmt.setString(2, courseTitle);
                pstmt.setString(3, deptName);
                pstmt.setInt(4, credits);
                pstmt.executeUpdate();
                System.out.println("Course added successfully.");
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }
    }

    // Retrieve all courses and their info (course_id, title, dept_name, credits)
    // and print them to the console
    private static void retrieveCourses(Connection conn) throws SQLException {
        // use prepared statement to retrieve course info
        String sql = "SELECT course_id, title, dept_name, credits FROM course";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nCourses\n--------------------");
            while (rs.next()) {
                String courseId = rs.getString("course_id");
                String title = rs.getString("title");
                String deptName = rs.getString("dept_name");
                int credits = rs.getInt("credits");
                System.out.println("Course ID: " + courseId + "\nTitle: " + title + "\nDepartment: " + deptName + "\nCredit Hours: " + credits + "\n");
            }
        }
    }


    // Delete a selected course from the database
    private static void deleteCourse(Connection conn, Scanner scan) throws SQLException {
        String courseId = "";
        boolean validCourseId = false;
        while (!validCourseId) {
            System.out.print("Enter course ID to delete: ");
            courseId = scan.nextLine();
            if (courseId == null || courseId.trim().isEmpty()) {
                System.out.println("Invalid input, please enter a valid course ID.");
            } else {
                validCourseId = true;
            }
        }
        // use prepared statement to delete selected course
        String sql = "DELETE FROM course WHERE course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseId);
            int rowsDeleted = pstmt.executeUpdate();
            System.out.println(rowsDeleted + " row(s) deleted");
        } catch (SQLIntegrityConstraintViolationException integ_const)
        {
            System.out.print("Cannot delete a course that is a prerequisite to another course.\n");
        }
    }


    // Update a selected course in the database
    private static void updateCourse(Connection conn, Scanner scan) throws SQLException {
        System.out.print("Enter course ID to update: ");
        String courseId = scan.nextLine();
        String choice1 = "";
        String choice2 = "";
        String newTitle = "";
        String creditsStr = "";
        Integer newCredits = null;
        // choose what to update (title and/or credits)
        System.out.print("Update course title? (Y/N) ");
        choice1 = scan.nextLine();
        while (!choice1.equalsIgnoreCase("Y") && !choice1.equalsIgnoreCase("N"))
        {
            System.out.print("Invalid input. Choose (Y/N) ");
            choice1 = scan.nextLine();
        }
        if (choice1.equalsIgnoreCase("Y"))
        {
            System.out.print("Enter new course title: ");
            newTitle = scan.nextLine();
        }
        System.out.print("Update number of credits? (Y/N) ");
        choice2 = scan.nextLine();
        while (!choice2.equalsIgnoreCase("Y") && !choice2.equalsIgnoreCase("N"))
        {
            System.out.print("Invalid input. Choose (Y/N) ");
            choice2 = scan.nextLine();
        }
        if (choice2.equalsIgnoreCase("Y"))
        {
            System.out.print("Enter new number of credits: ");
            creditsStr = scan.nextLine();
        }
        else
        {
            // set to 1 so doesn't enter next while
            newCredits = 1;
        }
        // newCredits hasn't been converted to int yet OR invalid input (negative)
        while ((newCredits == null && !creditsStr.isEmpty()) || newCredits <= 0)
        {
            try {
                newCredits = Integer.parseInt(creditsStr);
                // check for constraint violation (credits must be > 0)
                if (newCredits <= 0) {
                    System.out.println("Invalid input, please enter a positive integer.");
                    creditsStr = scan.nextLine();
                }
                // catch for noninteger input
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer value.");
                System.out.print("Enter new number of credits: ");
                creditsStr = scan.nextLine();
            }
        }

        // if user actually chose to update something, then perform update
        if (choice1.equalsIgnoreCase("Y"))
        {
            // use prepared statement to update course with user input
            String sql = "UPDATE course SET title = COALESCE(?, title) WHERE course_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newTitle);
                pstmt.setString(2, courseId);
                int upd = pstmt.executeUpdate();
            }
        }
        if (choice2.equalsIgnoreCase("Y"))
        {
            String sql = "UPDATE course SET credits = COALESCE(?, credits) WHERE course_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (newCredits == null) {
                    pstmt.setNull(1, Types.INTEGER);
                } else {
                    pstmt.setInt(1, newCredits);
                }
                pstmt.setString(2, courseId);
                int upd = pstmt.executeUpdate();
            }
        }
        // otherwise, print "Nothing updated"
        if (choice1.equalsIgnoreCase("N") && choice2.equalsIgnoreCase("N"))
        {
            System.out.println("Nothing updated.\n");
        }
    }
}
