package store.postgres;

import model.User;
import model.Post;
import model.Comment;
import store.model.CommentStore;
import utility.ResetDao;

import org.jdbi.v3.core.Jdbi;

public class CommentStorePostgres implements CommentStore {

    // Connection test function
    public static void main( String[] args ) {

        Jdbi jdbi = GreetDoughJdbi.create("jdbc:postgresql://localhost:4321/greetdough");
        UserStorePostgres UserStorePostgres = new UserStorePostgres(jdbi);
        PostStorePostgres PostStorePostgres = new PostStorePostgres(jdbi);
        CommentStorePostgres CommentStorePostgres = new CommentStorePostgres(jdbi);

        // Used to DROP and CREATE all tables
        ResetDao.reset(jdbi);

        User yeet = UserStorePostgres.addUser("yeet");

        // Add a post
        Post yeetPost = PostStorePostgres.addPost( "first!", yeet.getID() );

        // Create another post
        PostStorePostgres.addPost( "lol", yeet.getID() );

        // Create a comment (can't delete individually)
        System.out.println(CommentStorePostgres.canComment(yeetPost.getID()));
        Comment yeetCommentOne = CommentStorePostgres.insertComment("haha croissant", yeet.getID(), yeetPost.getID() );
        Comment yeetCommentTwo = CommentStorePostgres.insertComment("nawrrr", yeet.getID(), yeetPost.getID());

        // Reply to a comment
        //System.out.println(CommentStorePostgres.canReply(yeetCommentTwo.getID()));
        //CommentStorePostgres.insertComment("i love jlab", yeet.getID(), yeetPost.getID(), yeetCommentTwo.getID());

        // Get the list of parent comments under a post
        Comment yeetPostParents = CommentStorePostgres.getParents(yeetPost.getID());
        System.out.println(yeetPostParents);

        // Get the list of replies under a parent comment
        //CommentStorePostgres.getReplies(yeetCommentTwo.getID());

        // Delete users deletes the table
        UserStorePostgres.deleteUser( yeet.getID() );

    }

    // given a postid
    // return with hierarchy
    // single depth replies

    // canComment() check if post_id exists

    // on frontend you choose post and then choose a comment to reply to
    // requires boolean canReply()
    // checks if the given comment_id exists
    // true
    // feed the parentID, the comment i'm trying to reply to
    // insertComment(uid, content, parentID)
    // insert uid, content, parentID  (commentID auto generated key)
    // false
    // return status error, comment doesnt exist

    // regular comment
    // insertComment(uid, content, null)

    // Columns: commentID, uid, content, parentID
    // commentID -> another commentID (parentID)


    private final Jdbi jdbi;

    public CommentStorePostgres( final Jdbi jdbi ) {
        this.jdbi = jdbi;
    }

    public void delete() {
        jdbi.useHandle(handle -> handle.attach(CommentDao.class).deleteTable());
    };

    public void init() {
        jdbi.useHandle(handle -> handle.attach(CommentDao.class).createTable());
    };

    @Override
    public Comment getComment( int ID ) {
        return null;
    }

    @Override
    public Comment addComment( String contents, int uid ) {

        return null;

    }

    @Override
    public boolean canComment( int pid ) {

        return jdbi.withHandle( handle -> handle.attach(CommentDao.class).canComment(pid) );

    }

    @Override
    public Comment insertComment( String contents, int uid, int post_id, Integer parent_id ) {

        int ID = jdbi.withHandle( handle -> handle.attach(CommentDao.class).insertComment(uid, contents, post_id, parent_id) );
        return getComment(ID);

    }

    @Override
    public Comment insertComment( String contents, int uid, int post_id ) {

        return insertComment( contents, uid, post_id, 0 );

    }

    @Override
    public boolean canReply( int cid ) {

        return jdbi.withHandle( handle -> handle.attach(CommentDao.class).canReply(cid) );

    }

    @Override
    public Comment getReplies(int parent_comment_id) {
        return jdbi.withHandle( handle -> handle.attach(CommentDao.class).getReplies(parent_comment_id) );
    }

    @Override
    public Comment getParents(int post_id) {
        return jdbi.withHandle( handle -> handle.attach(CommentDao.class).getParents(post_id) );
    }

    /*
    identify a post
        make a list of every parent_id
            // array_agg(parent_id) where post_id = (:post_id)
        in handler loop through each parent_id
            call getReplies

    comments section:
        parent 1
            replies
            replies
            replies
        parent 2
            replies
        parent 3
        parent 4
            replies
    */

}

