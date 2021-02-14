import com.fasterxml.jackson.databind.ObjectMapper;

import static spark.Spark.*;
import java.io.*;
import java.util.HashMap;
import java.util.Stack;

public class Server {

    ////////////////// File Paths //////////////////
    private static final String PATH_TO_USER = "/users/";
    private static final String PATH_TO_USER_ID = PATH_TO_USER + ":id";

    ////////////////// Members //////////////////
    private static ObjectMapper mapper = new ObjectMapper();
    private static UtilityID recordID = new UtilityID();

    ////////////////// Functions //////////////////
    public static int getUnusedUserID() {
        return recordID.getUnusedUserID();
    }

    public static int getUnusedPostID() {
        return recordID.getUnusedPostID();
    }

    public static void addUnusedUserID( int ID ) {
        recordID.addUnusedUserID(ID);
    }

    public static void addUnusedPostID( int ID ) {
        recordID.addUnusedPostID(ID);
    }



    /////////// NEED TO SAVE STACKS BEFORE SERVER SHUTDOWN
    public static void main(String[] args) {

        initExceptionHandler((e) -> {
            System.out.println("Could not start server on port 9999");
            System.exit(100);
        });
        port(9999);
        init();

        // Load the saved users
        Server.loadUsers();

        ////// Also need to load the stacks
        ////// Alternatively, can iterate thru all saved users and push missing IDs

        // you can send requests with curls.
        // curl -X POST localhost:9999/users/*id*

        // USER ROUTES
        // Returns the user id
        get( Server.PATH_TO_USER_ID, (req, res) -> {

            HashMap<Integer,User> userHashMap = loadUsers();
            int id = Integer.parseInt( req.params(":id") );

            return userHashMap.get(id);

        });

        post( Server.PATH_TO_USER, (req, res) -> {

            // Creates a new user into database or wherever
            // curl -d “name=Tony Belladonna” -X post localhost:9999/users/

            String name = req.queryParams("name");
            User tempUser = new User(name);
            System.out.println( "Creating a user: " + tempUser.getName() + ", " + tempUser.getID() );

            // Save target user to server
            HashMap<Integer,User> userHashMap = loadUsers();
            userHashMap.put( tempUser.getID(), tempUser );
            saveUsers(userHashMap);

            return Server.mapper.writeValueAsString(userHashMap);

        });

        // Update the user. Needs a lot of options.
        put( Server.PATH_TO_USER_ID, (req,res) -> {
            return "Updating a user: " + req.params(":id");
        });

        // Deletes user
        delete( Server.PATH_TO_USER_ID, (req,res) -> {

            HashMap<Integer,User> userHashMap = loadUsers();
            int id = Integer.parseInt( req.params(":id") );

            // Check if the ID is valid
            assert id >= 0: "Invalid ID.";
            assert userHashMap.containsKey(id): "User does not exist.";

            // Delete target user dependencies
            User targetUser = userHashMap.get(id);
            targetUser.deleteUser();

            // Remove target user from server
            userHashMap.remove(id);
            saveUsers(userHashMap);

            System.out.println( "Deleted a user: " + targetUser.getName() + ", " + targetUser.getID() );

            return Server.mapper.writeValueAsString(userHashMap);

        });

    }

    // Helper Functions
    private static HashMap<Integer,User> loadUsers() {

        HashMap<Integer,User> userHashMap = new HashMap<>();

        try {

            FileInputStream fi = new FileInputStream( new File("data/users.txt") );
            ObjectInputStream oi = new ObjectInputStream(fi);

            if( fi.available() != 0 ) {
                userHashMap = (HashMap<Integer,User>) oi.readObject();
                oi.close();
                fi.close();
            }

        } catch( Exception ex ) {
            ex.printStackTrace();
        }

        return userHashMap;
    }

    private static Integer saveUsers( HashMap<Integer,User> mapToSave ) {

        try {

            FileOutputStream fo = new FileOutputStream(new File("data/users.txt"), false);
            ObjectOutputStream oo = new ObjectOutputStream(fo);

            oo.writeObject(mapToSave);
            oo.flush();
            oo.close();
            fo.close();

        } catch ( Exception ex ) {
            ex.printStackTrace();
            return -1;
        }

        return 0;
    }


}