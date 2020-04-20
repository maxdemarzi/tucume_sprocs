package me.tucu.posts;

import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import org.neo4j.graphdb.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static me.tucu.schema.DatedRelationshipTypes.POSTED_ON;
import static me.tucu.schema.DatedRelationshipTypes.REPOSTED_ON;
import static me.tucu.schema.Properties.*;
import static me.tucu.utils.Time.dateFormatter;

public class Posts {

    public static boolean userRepostedPost(Transaction tx, Node user, Node post) {

        // It's an advertisement
        if(post.hasRelationship(RelationshipTypes.PROMOTES)) {

            Long postId = post.getId();
            String username = (String)user.getProperty(USERNAME);
            ResourceIterator<Node> iterator = tx.findNodes(Labels.Post, USERNAME, username, POST_ID, postId);
            return iterator.hasNext();
        }

        // It's a regular post

        // If the post has a few incoming relationships, just brute force it
        if (post.getDegree(Direction.INCOMING) < 1000) {
            for (Relationship r1 : post.getRelationships(Direction.INCOMING)) {
                if (r1.getStartNode().equals(user) && r1.getType().name().startsWith(REPOSTED_ON)) {
                    return true;
                }
            }
        }

        // If the post has lots of relationships, start from now and go backwards
        // until the post creation date checking it or the user for a repost relationship
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime time = (ZonedDateTime)post.getProperty(TIME);
        while(now.isAfter(time)) {
            RelationshipType repostedOn = RelationshipType.withName(REPOSTED_ON +
                    now.format(dateFormatter));

            if (user.getDegree(repostedOn, Direction.OUTGOING)
                    < post.getDegree(repostedOn, Direction.INCOMING)) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, repostedOn)) {
                    if (r1.getEndNode().equals(post)) {
                        return true;
                    }
                }
            } else {
                for (Relationship r1 : post.getRelationships(Direction.INCOMING, repostedOn)) {
                    if (r1.getStartNode().equals(user)) {
                        return true;
                    }
                }
            }
            // Check the day before
            now = now.minusDays(1);
        }

            return false;
    }

    public static Node getAuthor(Node post) {
        ZonedDateTime time = (ZonedDateTime)post.getProperty(TIME);
        RelationshipType original = RelationshipType.withName(POSTED_ON +
                time.format(dateFormatter));
        return post.getSingleRelationship(original, Direction.INCOMING).getStartNode();
    }

    public static Long getRepostedCount(Node post) {
        long count = 0;

        // It's a regular post
        if(!post.hasRelationship(RelationshipTypes.PROMOTES)) {
            return (long) (post.getDegree(Direction.INCOMING)
                    - 1 // for the Posted Relationship Type
                    - post.getDegree(RelationshipTypes.LIKES)
                    - post.getDegree(RelationshipTypes.REPLIED_TO));
        }

        // It's an advertisement
        ArrayList<Node> posts = new ArrayList<>();
        posts.add(post);

        while (!posts.isEmpty()) {
            Node node = posts.remove(0);
            for (Relationship rel : node.getRelationships(Direction.INCOMING, RelationshipTypes.REPOSTED)) {
                count++;
                posts.add(rel.getStartNode());
            }
        }

        return count;
    }

    public static Node getOriginalPost(Node post) {
        while(post.hasRelationship(Direction.OUTGOING, RelationshipTypes.REPOSTED)) {
            post = post.getSingleRelationship(RelationshipTypes.REPOSTED, Direction.OUTGOING).getEndNode();
        }
        return post;
    }

}
