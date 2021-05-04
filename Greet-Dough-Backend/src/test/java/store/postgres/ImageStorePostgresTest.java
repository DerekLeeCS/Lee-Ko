package store.postgres;

import model.Image;
import model.Post;
import model.User;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import utility.ResetDao;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageStorePostgresTest extends ImageStorePostgres {

    private static final Jdbi jdbi = GreetDoughJdbi.create("jdbc:postgresql://localhost:4321/greetdough");

    public ImageStorePostgresTest() {
        super(jdbi);
    }

    @Test
    void test() {

        UserStorePostgres userStorePostgres = new UserStorePostgres(jdbi);
        PostStorePostgres postStorePostgres = new PostStorePostgres(jdbi);
        ImageStorePostgres imageStorePostgres = new ImageStorePostgres(jdbi);

        // Used to DROP and CREATE all tables
        ResetDao.reset(jdbi);

        User newUser = userStorePostgres.addUser("Komodo");

        // Test empty returns
        assert ( imageStorePostgres.getAllImages().isEmpty() );
        assertNull( imageStorePostgres.getImage(1) );

        // Get local image
        FileSystem fileSys = FileSystems.getDefault();
        Path tempPath = fileSys.getPath( System.getProperty("user.dir") );
        for ( int a=0; a<3; a++ ) {
            tempPath = tempPath.getParent();
        }
        Path newPath = fileSys.getPath( tempPath.toString() + File.separator + "beardKoolmodo.png" );

        // Test copying and saving an image
        Image selfie = imageStorePostgres.addImage( newUser.getID(), newPath.toString(), false );
        List<Integer> iidList = new LinkedList<>();
        iidList.add( selfie.getID() );

        // Test adding a post with an image
        String title = "Feeling cute, might delete later";
        String contents = "first!";
        Post newPost = postStorePostgres.addPost( title, contents, newUser.getID(), iidList );
        assert ( newPost.getUserID() == newUser.getID() );
        assert ( newPost.getImageIDList().equals( iidList ) );
        assert ( newPost.getTitle().equals( title ) );
        assert ( newPost.getContents().equals( contents ) );

        // Test retrieving the image
        assert ( imageStorePostgres.getImage( selfie.getID() ).getPath().equals( selfie.getPath() ) );

        // Test soft deleting and then clearing an image
        Image tempImage = imageStorePostgres.addImage( newUser.getID(), newPath.toString(), false );
        assert ( imageStorePostgres.getAllImages().size() == 2 );

        // Check that the number of rows is still the same
        imageStorePostgres.deleteImage( tempImage.getID() );
        assert ( imageStorePostgres.getAllImages().size() == 2 );

        // Check that the soft deleted row has been removed
        imageStorePostgres.clearDeleted();
        assert ( imageStorePostgres.getAllImages().size() == 1 );

        // Test deleting the user
        //      Should delete cascade the image
        userStorePostgres.deleteUser( newUser.getID() );
        assertNull ( imageStorePostgres.getImage( selfie.getID() ) );

        // Check that the soft deleted row has been removed
        imageStorePostgres.clearDeleted();
        assert ( imageStorePostgres.getAllImages().size() == 0 );

    }

}