package store.postgres;

import model.User;
import model.Post;
import model.Likes;
import store.model.LikeStore;
import utility.ResetDao;

import org.jdbi.v3.core.Jdbi;

import java.util.HashSet;

public class LikeStorePostgres implements LikeStore {

    // test function
    public static void main( String[] args ) {

        Jdbi jdbi = GreetDoughJdbi.create("jdbc:postgresql://localhost:4321/greetdough");
        UserStorePostgres UserStorePostgres = new UserStorePostgres(jdbi);
        PostStorePostgres PostStorePostgres = new PostStorePostgres(jdbi);
        LikeStorePostgres LikeStorePostgres = new LikeStorePostgres(jdbi);

        // Used to DROP and CREATE all tables
        ResetDao.reset(jdbi);

        User newUser = UserStorePostgres.addUser("Felipe");
        User tempUser = UserStorePostgres.addUser("Jill");

        // Make two posts
        Post newPost = PostStorePostgres.addPost( "first!", newUser.getID() );
        Post secondPost = PostStorePostgres.addPost( "haha very cool!", newUser.getID() );

        // Like one post
        LikeStorePostgres.addUserLike( newPost.getID(), newUser.getID() );
        LikeStorePostgres.addUserLike( newPost.getID(), tempUser.getID() );

        // Check who liked the post
        System.out.println( LikeStorePostgres.getLikes( newPost.getID() ).getUserLikes() );

        // Count how many people liked a post

        // A single user unlikes a post
        LikeStorePostgres.removeUserLike(newPost.getID(), newUser.getID());

        // Check if the user liked a specific post
        System.out.println( LikeStorePostgres.hasUserLike( newPost.getID(), newUser.getID() ) );

        // Delete post => delete all likes
        PostStorePostgres.deletePost( newPost.getID() );
        System.out.println( LikeStorePostgres.getLikes( newPost.getID() ) );

    }

    private final Jdbi jdbi;

    public LikeStorePostgres( final Jdbi jdbi ) {
        this.jdbi = jdbi;
    }

    public void delete() {
        jdbi.useHandle(handle -> handle.attach(LikeDao.class).deleteTable());
    }

    public void init() {
        jdbi.useHandle(handle -> handle.attach(LikeDao.class).createTable());
    }

    @Override
    public Likes getLikes( int pid ) {

        HashSet<Integer> userLikes = jdbi.withHandle(handle -> handle.attach(LikeDao.class).getUserLikes(pid) );

        // If userLikes is empty, return a null object
        // Else, return the Likes object
        return userLikes.size() != 0 ? new Likes( pid, userLikes ) : null;

    }

    @Override
    public void addUserLike( int pid, int uid ) {
        jdbi.useHandle( handle -> handle.attach(LikeDao.class).insertLikes(pid, uid) );
    }

    @Override
    public void removeUserLike( int pid, int uid ) {
        jdbi.useHandle( handle -> handle.attach(LikeDao.class).deleteUserLike(pid, uid) );
    }

    @Override
    public void deleteLikes( int pid ) {
//        jdbi.useHandle( handle -> handle.attach(LikeDao.class).deleteLikes(pid));
    }

    @Override
    public boolean hasUserLike( int pid, int uid ){
        return jdbi.withHandle( handle -> handle.attach(LikeDao.class).containsLike(pid, uid) );
    }

}
