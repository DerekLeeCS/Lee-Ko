import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import model.*;
import utility.Pair;
import store.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;

public class Handler {

    private final UserStore userStore;
    private final PostStore postStore;
    private final ImageStore imageStore;
    private final LikeStore likeStore;
    private final SubStore subStore;
    private final FollowStore followStore;
    private final PostCommentStore postCommentStore;
    private final CommentStore commentStore;
    private final PasswordStore passwordStore;
    private final LoginStore loginStore;
    private final WalletStore walletStore;
    private final Gson gson = new Gson();
    ObjectMapper mapper = new ObjectMapper();

    public Handler(UserStore userStore,
                   PostStore postStore,
                   ImageStore imageStore,
                   LikeStore likeStore,
                   CommentStore commentStore,
                   SubStore subStore,
                   FollowStore followStore,
                   PostCommentStore postCommentStore,
                   PasswordStore passwordStore,
                   LoginStore loginStore,
                   WalletStore walletStore) {
                       
        this.userStore = userStore;
        this.postStore = postStore;
        this.imageStore = imageStore;
        this.likeStore = likeStore;
        this.commentStore = commentStore;
        this.subStore = subStore;
        this.followStore = followStore;
        this.postCommentStore = postCommentStore;
        this.passwordStore = passwordStore;
        this.loginStore = loginStore;
        this.walletStore = walletStore;

    }

    // PRIVATE HELPER FUNCTIONS
    private Pair grabUserPair( Request req ) {

        int uid = Integer.parseInt( req.queryParams("uid") );
        int targetUser = Integer.parseInt( req.params(":id") );

        if ( userStore.getUser(uid) == null ) {

            System.err.println("Current user " + uid + " does not exist");
            return null;

        }
        if ( userStore.getUser(targetUser) == null ) {

            System.err.println("Target user " + targetUser + " does not exist");
            return null;

        }

        return new Pair(uid, targetUser);

    }

    private int checkUserPostPerms( int uid, int pid ) {

        if ( !postStore.hasPost(pid) ) {

            System.err.println("Post does not exist");
            return 404;

        }

        if ( !userStore.hasUser(uid) ) {

            System.err.println("User does not exist");
            return 404;

        }

        ArrayList<Integer> subs = subStore.getSubscriptions(uid);
        int tuid = postStore.getPost(pid).getUserID();

        return ( (subs != null) && (subs.contains(tuid)) ? 200 : 403 );

    }

    // USER ACTIONS
    public String getUser( Request req, Response res ) throws JsonProcessingException {

        int uid = Integer.parseInt( req.params(":uid") );

        if ( userStore.hasUser(uid) ) {
            
            res.status(200);
            String userJSON = mapper.writeValueAsString( userStore.getUser(uid) );
            return userJSON;

        } else {

            res.status(404);
            return null;

        }
    }

    public int createUser( Request req, Response res ) {

        res.type("application/json");
        Properties data = gson.fromJson(req.body(), Properties.class);
        String email = data.getProperty("email");
        String username = data.getProperty("username");
        String password = data.getProperty("password");
        System.out.println(email + ", " + username + ", " + password);

        // Check if email has been used already
        if ( passwordStore.hasEmail(email) ) {

            res.status(409);
            return res.status();

        }

        User tempUser = userStore.addUser(username);

        // Attempt to add a password associated with the email
        //      If return value is 0, attempt was unsuccessful
        if ( passwordStore.addPassword(email, tempUser.getID(), password ) == 0 ) {

            System.err.println( "Cannot add password for email" + email );
            res.status(409);
            return res.status();

        }

        System.out.println( "User Created: " + tempUser.getName() + ", " + tempUser.getID() );
        System.out.println( "PASSWORD STORED\n" );

        res.status(200);
        return res.status();

    }

    public int deleteUser( Request req, Response res ) {

        int uid = Integer.parseInt( req.params(":id") );
        User tempUser = userStore.getUser(uid);

        userStore.deleteUser(uid);

        // Checks if user was deleted
        if ( !userStore.hasUser(uid) ) {

            System.out.println( gson.toJson(tempUser) );
            res.status(200);

        } else {
            res.status(404);
        }

        return res.status();

    }

    public String tokenToID( Request req, Response res ) {

        res.type("application/json");
        Properties data = gson.fromJson(req.body(), Properties.class);
        String token = data.getProperty("authToken");

        Integer uid = loginStore.getUserID(token);

        if ( uid == null ) {

            res.status(401);
            return "";

        }

        // Not sure if this body part is required, since we are returning uid?
        JsonObject uidJSON = new JsonObject();
        uidJSON.addProperty("uid", uid);
        res.body( String.valueOf(uidJSON) );

        res.status(200);
        return res.body();

    }

    public String login( Request req, Response res ) {

        res.type("application/json");
        Properties data = gson.fromJson(req.body(), Properties.class);
        String email = data.getProperty("email");
        String password = data.getProperty("password");

        System.out.println("Logging in: " + email +", "+ password);

        // Check if login was successful
        Integer uid = passwordStore.getUserID(email, password);
        if ( uid == null ) {

            res.status(403);
            System.err.println("Unsuccessful login!");
            return "";

        }

        System.out.println(uid + " Logged in!");

        String cookie = loginStore.addSession(uid);
        JsonObject cookieJSON = new JsonObject();
        cookieJSON.addProperty("authToken", cookie);
        res.body( String.valueOf(cookieJSON) );

        System.out.println( res.body() );
        res.status(200);
        return res.body();

    }

    public String getUserFeed( Request req, Response res ) throws JsonProcessingException {

        res.type("application/json");
        int uid = Integer.parseInt( req.params(":uid") );

//        Properties data = gson.fromJson(req.body(), Properties.class);
//        Integer cuid = Integer.parseInt( data.getProperty("cuid") );

        System.out.println("Doing logic stuff now");
        if ( userStore.hasUser(uid) ) {

            List<Post> feed = postStore.makeFeed(uid);
            res.status(200);
            return mapper.writeValueAsString( feed );

        } else {

            res.status(404);
            return "";

        }
    }



    // USER RELATION ACTIONS

//    public int subscribe( Request req, Response res ) {
//
//        Pair userPair = this.grabUserPair(req);
//
//        if ( userPair == null ) {
//
//            res.status(404);
//            return res.status();
//
//        }
//
//        List<Integer> curSubs = subStore.getSubscriptions(userPair.getLeft());
//
//        if ( curSubs != null && curSubs.contains( userPair.getRight() ) ) {
//
//            System.err.println("Current user already is subscribed");
//            res.status(404);
//            return res.status();
//
//        }
//
//        subStore.addSubscription( userPair.getLeft(), userPair.getRight() );
//        System.out.println( "current subs: " + subStore.getSubscriptions(userPair.getLeft()) );
//
//        return 0;
//
//    }

//    public int unsubscribe( Request req, Response res ) {
//
//        Pair userPair = this.grabUserPair(req);
//        if ( userPair == null ) {
//
//            res.status(404);
//            return res.status();
//
//        }
//
//        List<Integer> curSubs = subStore.getSubscriptions( userPair.getLeft() );
//
//        if ( curSubs == null ) {
//
//            System.err.println("User not subscribed to anyone");
//            res.status(404);
//            return res.status();
//
//        }
//
//        if ( !curSubs.contains(userPair.getRight()) ) {
//            System.err.println("Current user not subscribed to target user");
//        }
//
//        subStore.removeSubscription( userPair.getLeft(), userPair.getRight() );
//        System.out.println( subStore.getSubscriptions(userPair.getRight()) );
//        res.status(200);
//        return 0;
//
//    }

/*
    public Integer follow( Request req ) {

        Pair userPair = this.grabUserPair(req);
        if ( userPair == null ) {
            return null;
        }
        List<Integer> curFollows = followStore.getFollowers( userPair.getLeft() );

        if ( curFollows!=null && curFollows.contains( userPair.getRight() ) ) {
            System.out.println("Current user is already following the target user");
            return null;
        }

        followStore.addFollower( userPair.getLeft(), userPair.getRight() );
        IOservice.saveObject(followStore, "data/follows.txt");
        System.out.println("Currently following: " + followStore.getFollowers( userPair.getLeft() ) );

        return 0;

    }

    public Integer unfollow( Request req ) {

        Pair userPair = this.grabUserPair(req);
        if ( userPair == null ) {
            return null;
        }
        List<Integer> curFollows = followStore.getFollowers( userPair.getLeft() );

        if ( curFollows == null ) {
            System.out.println("Current user not following anyone");
            return null;
        }

        if ( !curFollows.contains( userPair.getRight()) ) {
            System.out.println("Current user not following target user");
            return null;
        }

        followStore.removeFollower( userPair.getLeft(), userPair.getRight() );
        IOservice.saveObject(followStore, "data/follows.txt");
        System.out.println("Currently following: " + followStore.getFollowers( userPair.getLeft() ) );

        return 0;

    }

 */

    // POST ACTIONS
    public Post getPost( Request req, Response res ) {

        int pid = Integer.parseInt( req.params(":id") );

        if ( postStore.hasPost(pid) ) {

            res.status(200);
            System.err.println("Post does not exist");
            return postStore.getPost(pid);

        } else {

            res.status(404);
            return null;

        }

    }

    public int createPost( Request req, Response res ) {

        res.type("application/json");
        Properties data = gson.fromJson(req.body(), Properties.class);

        int uid = Integer.parseInt( data.getProperty("uid") );

        String imageQuery = data.getProperty("imageQuery");

        String contents = data.getProperty("contents");
        Integer iid = (imageQuery != null) ? Integer.parseInt(imageQuery) : null;

        if ( !userStore.hasUser(uid) ) {

            res.status(404);
            System.err.println("User does not exist");
            return res.status();

        }

        Post tempPost = postStore.addPost( contents, uid, iid );

        System.out.println( gson.toJson(tempPost) );

        res.status(200);
        return res.status();

    }

    public Integer deletePost( Request req, Response res ) {

        int pid = Integer.parseInt( req.params(":id") );
        Post tempPost = postStore.getPost(pid);

        // Should cascade delete the image, comments, likes, etc.
        postStore.deletePost(pid);

        // Checks if the post was deleted
        if ( !postStore.hasPost(pid) ) {

            System.out.println( gson.toJson(tempPost) );
            res.status(200);

        } else {
            res.status(404);
        }


        return res.status();


    }

//    public List<Post> getFeed( Request req, Response res ) {
//
//        int uid = Integer.parseInt( req.queryParams("uid") );
//        int tuid = Integer.parseInt( req.params(":id") );
//        List<Integer> curSubs = subStore.getSubscriptions(uid);
//
//        if ( !userStore.hasUser(uid) || !userStore.hasUser(tuid) ) {
//
//            res.status(404);
//            return null;
//
//        }
//
//        if ( (curSubs == null) || (!curSubs.contains(tuid)) ) {
//
//            System.out.println("Current user does not have permission to this feed");
//            res.status(403);
//            return null;
//
//        }
//
//        return postStore.makeFeed(tuid);
//
//    }

    public HashSet<Integer> getLikes( Request req, Response res ) {

        int uid = Integer.parseInt( req.queryParams("uid") );
        int pid = Integer.parseInt( req.params(":postID") );
        int status = checkUserPostPerms(uid, pid);
        res.status(status);

        if ( res.status() == 200 ) {
            return likeStore.getLikes(pid).getUserLikes();
        }

        System.err.println("Error code: " + res.status() );
        return null;

    }

    /*
    public Likes getLikes( Request req, Response res ) {

        int uid = Integer.parseInt( req.queryParams("uid") );
        int pid = Integer.parseInt( req.params(":postID") );
        int status = checkUserPostPerms(uid, pid);
        res.status(status);

        if ( res.status() == 200 ) {
            return likeStore.getLikes(pid);
        }

        System.err.println("Error code: " + res.status() );
        return null;

    }
    */

    public Integer likePost( Request req, Response res ) {

        int pid = Integer.parseInt( req.params(":postID") );
        int uid = Integer.parseInt( req.queryParams("uid") );
        int status =  checkUserPostPerms(uid, pid);
        res.status(status);

        if ( res.status() == 200 ) {

            if ( !likeStore.hasUserLike(pid, uid) ) {
                likeStore.addUserLike(pid, uid);
            } else {
                likeStore.deleteUserLike(pid, uid);
            }

        } else {
            System.err.println("Error code: " + res.status());
        }

        return res.status();

    }

    /*
    public Integer likePost( Request req, Response res ) {

        int pid = Integer.parseInt( req.params(":postID") );
        int uid = Integer.parseInt( req.queryParams("uid") );
        int status = checkUserPostPerms(uid, pid);
        Likes postLikes = likeStore.getLikes(pid);
        res.status(status);

        if ( res.status() == 200 ) {

            HashSet<Integer> userLikes = postLikes.getUserLikes();

            if ( !userLikes.contains(uid) ) {

                postLikes.incrementLike(uid);
                System.out.println( gson.toJson(postLikes) );

            }

        } else {
            System.err.println("Error code: " + res.status());
        }

        return res.status();

    }
     */

    public Integer createComment( Request req, Response res ) {

        int pid = Integer.parseInt( req.params(":postID") );
        int uid = Integer.parseInt( req.queryParams("uid") );
        String contentQuery = req.queryParams("contents");
        int status = checkUserPostPerms(uid, pid);

        Properties data = gson.fromJson(req.body(), Properties.class);
        String parent = data.getProperty("parent_id");
        int parent_id = (parent != null) ? Integer.parseInt(parent) : -1;

        res.status(status);

        if ( res.status() == 200 ) {

            if ( !commentStore.hasParent(parent_id) ) {
                // need to check if parent is a parent not a reply, to ensure depth 1
                if ( commentStore.isParent(parent_id) ) {
                    commentStore.addComment( contentQuery, uid, pid, parent_id );
                }
                else {
                    System.err.println("Error code: " + res.status());
                }
            } else {
                commentStore.addComment( contentQuery, uid, pid );
            }

        } else {
            System.err.println("Error code: " + res.status());
        }

        return res.status();
    }

    /*
    public Integer createComment( Request req, Response res ) {

        int pid = Integer.parseInt( req.params(":postID") );
        int uid = Integer.parseInt( req.queryParams("uid") );
        String contentQuery = req.queryParams("contents");
        int status = checkUserPostPerms(uid, pid);
        res.status(status);

        if ( res.status() == 200 ) {

            Comment newComment = commentStore.addComment( contentQuery, uid, pid );
            postCommentStore.addComment( pid, newComment.getID() );

            System.out.println( gson.toJson(newComment) );

        } else {
            System.err.println("Error code: " + res.status());
        }

        return res.status();
    }
    */


    // havent checked permissions for this yet

    public List<Comment> getParentComments( Request req, Response res ) {

        int pid = Integer.parseInt( req.params(":postID") );
        int uid = Integer.parseInt( req.queryParams("uid") );
        int status = checkUserPostPerms(uid, pid);
        List<Comment> comments = new ArrayList<>();
        res.status(status);

        if ( res.status() == 200 ) {
            return commentStore.getParents(pid);
        } else {
            System.err.println("Error code: " + res.status());
        }

        return comments;

    }

    public List<Comment> getRepliesComments( Request req, Response res ) {

        int pid = Integer.parseInt( req.params(":postID") );
        int uid = Integer.parseInt( req.queryParams("uid") );
        int cid = Integer.parseInt( req.queryParams("cid") );
        int status = checkUserPostPerms(uid, pid);
        List<Comment> comments = new ArrayList<>();
        res.status(status);

        if ( res.status() == 200 ) {
            return commentStore.getReplies(cid);
        } else {
            System.err.println("Error code: " + res.status());
        }

        return comments;

    }

//    public ArrayList<Comment> getComments( Request req, Response res ) {
//
//        int pid = Integer.parseInt( req.params(":postID") );
//        int uid = Integer.parseInt( req.queryParams("uid") );
//        int status = checkUserPostPerms(uid, pid);
//        ArrayList<Comment> comments = new ArrayList<>();
//        res.status(status);
//
//        if ( res.status() == 200 ) {
//
//            ArrayList<Integer> cidList = postCommentStore.getComments(pid);
//            for ( Integer ID : cidList ) {
//                comments.add( commentStore.getComment(ID) );
//            }
//
//        } else {
//            System.err.println("Error code: " + res.status());
//        }
//
//        return comments;
//
//    }


}



