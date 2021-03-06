package store.postgres;

import model.User;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utility.GreetDoughJdbi;
import utility.ResetDao;

import static org.junit.jupiter.api.Assertions.*;

class PasswordStorePostgresTest extends PasswordStorePostgres {

    private static final Jdbi jdbi = GreetDoughJdbi.create("jdbc:postgresql://localhost:4321/greetdough");

    private static UserStorePostgres userStorePostgres;
    private static ImageStorePostgres imageStorePostgres;
    private static PasswordStorePostgres passwordStorePostgres;

    private static User newUser;

    public PasswordStorePostgresTest() {
        super(jdbi);
    }

    @BeforeAll
    static void setUpAll() {

        // Delete all the databases (only use the relevant ones)
        ResetDao.deleteAll(jdbi);

        userStorePostgres = new UserStorePostgres(jdbi);
        imageStorePostgres = new ImageStorePostgres(jdbi);
        passwordStorePostgres = new PasswordStorePostgres(jdbi);

    }

    @AfterAll
    static void tearDownAll() {
        ResetDao.reset(jdbi);
    }

    @BeforeEach
    void setUpEach() {

        // Delete the databases
        passwordStorePostgres.delete();
        imageStorePostgres.delete();
        userStorePostgres.delete();

        // Initialize the databases
        userStorePostgres.init();
        imageStorePostgres.init();
        passwordStorePostgres.init();

        // Create a user
        newUser = userStorePostgres.addUser("B. Ryan");

    }

    @Test
    void testAddEmailPassword() {

        // Add the user with an associated email and password
        // Returns 0 if there is a duplicate email
        String email = "SweetNDough@gmail.com";
        String pass = "password123";
        assert ( passwordStorePostgres.addPassword( email, newUser.getID(), pass ) != 0 );

        // Check if the password is correct
        assert ( passwordStorePostgres.hasEmail(email) );
        assert ( passwordStorePostgres.getUserID(email, pass) == newUser.getID() );

        // Test adding a second password for the same email but lowercase
        assert ( passwordStorePostgres.addPassword( email.toLowerCase(), newUser.getID(), "lol" ) == 0 );

    }

    @Test
    void testChangeEmailPassword() {

        // Add the user with an associated email and password
        // Returns 0 if there is a duplicate email
        String email = "SweetNDough@gmail.com";
        String pass = "password123";
        assert ( passwordStorePostgres.addPassword( email, newUser.getID(), pass ) != 0 );

        // Test changing the email/password
        String newEmail = "WheatNGrow@aol.com";
        String newPass = "password1234";
        passwordStorePostgres.changeEmail( email, newEmail );
        passwordStorePostgres.changePassword( newEmail, newPass );

        // Check if update was successful
        assert ( passwordStorePostgres.hasEmail(newEmail) );
        assert ( passwordStorePostgres.getUserID( newEmail, newPass ) == newUser.getID() );

    }

    @Test
    void testDeleteUser() {

        // Add the user with an associated email and password
        // Returns 0 if there is a duplicate email
        String email = "SweetNDough@gmail.com";
        String pass = "password123";
        assert ( passwordStorePostgres.addPassword( email, newUser.getID(), pass ) != 0 );

        // Test deleting the user
        //      Should delete cascade the email and password
        userStorePostgres.deleteUser( newUser.getID() );
        assertFalse( passwordStorePostgres.hasEmail(email) );

    }

}