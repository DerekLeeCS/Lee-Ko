package model;

import utility.Pair;
import database.Server;

import java.util.ArrayList;
import java.util.HashSet;
import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    ////////////////// Members //////////////////
    private String name;
    private final int ID;                   // Stores unique id for a given user
    private int wallet;

    // Stores a list of a user's subscriptions
    // < Content_Creator_ID, Subscription_Tier >
    protected ArrayList<Pair> subscriptions;

    // Stores a list of a user's followers
    // Used to send notifications, emails, etc.
    protected HashSet<Integer> followers;

    ////////////////// Constructor //////////////////
    public User( String name ) {

        this.name = name;
        this.ID = Server.getUnusedUserID();
        this.subscriptions = new ArrayList<>();
        this.followers = new HashSet<>();
        this.wallet = 0;

    }

    ////////////////// Functions //////////////////
    public String getName() {
        return this.name;
    }

    public int getID() {
        return this.ID;
    }

    public ArrayList<Pair> getSubscriptions() {
        return this.subscriptions;
    }

    public HashSet<Integer> getFollowers() {
        return this.followers;
    }

    // Deletes this user
    public void deleteUser() {

        // Delete all of this user's subscriptions
        for ( Pair subscribed : subscriptions ) {

            int ID = subscribed.getLeft();

            // Access user thru id and update subscription's followers


        }

        // Delete this user from all of this user's followers
        // Potentially notify them??
        for ( int ID : followers ) {

            // Access user thru id and update follower's subscriptions

        }

    }

    // Subscribes to the target user given by ID
    // NEED TO CHECK THAT USER ISNT TRYING TO SUBSCRIBE TO SELF
    //      AND ISNT ALREADY SUBSCRIBED
    public void subscribe( int ID ) {

        // Adds the current user to the target user's followers
        User targetUser = Server.getUser(ID);
        targetUser.followers.add( this.getID() );

        // Add the target user to the current user's subscriptions
        Pair subInfo = new Pair( targetUser.getID(), 0 );
        this.subscriptions.add( subInfo );

    }

}