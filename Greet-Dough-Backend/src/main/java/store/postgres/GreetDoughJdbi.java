package store.postgres;

import model.Post;
import model.User;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GreetDoughJdbi {

    public static Jdbi create( String url ) {

        Jdbi jdbi = Jdbi.create( url, BaseDao.name, BaseDao.password )
                .installPlugin( new PostgresPlugin() )
                .installPlugin( new SqlObjectPlugin() );
//        jdbi.registerRowMapper(
//
//            // Same as
//            //      new RowMapper<User>()
//            (RowMapper<User>) (rs, ctx) -> {
//                int id = rs.getInt("id");
//                String name = rs.getString("name");
//                return new User( name, id );
//            }
//
//        );

        // you can register row mappers here or you can use @RegisterRowMapper annotation on each Dao
        // method
        jdbi.registerRowMapper( new UserRowMapper() );
        jdbi.registerRowMapper( new PostRowMapper() );

        return jdbi;

    }

    public static class UserRowMapper implements RowMapper<User> {

        @Override
        public User map( final ResultSet rs, final StatementContext ctx ) throws SQLException {

            int ID = rs.getInt("user_id");
            String name = rs.getString("name");

            return new User( name, ID );

        }

    }

    public static class PostRowMapper implements RowMapper<Post> {

        @Override
        public Post map( final ResultSet rs, final StatementContext ctx ) throws SQLException {

            int ID = rs.getInt("post_id");
            int userID = rs.getInt("user_id");
            Integer imageID = rs.getObject("image_id", Integer.class);
            String contents = rs.getString("contents");

            return new Post( contents, ID, userID, imageID );

        }

    }

}