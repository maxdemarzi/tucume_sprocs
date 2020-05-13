# Tucu.me Sprocs
Tucu.me Stored Procedures

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/tucume_sprocs-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/tucume_sprocs-1.0-SNAPSHOT.jar neo4j-enterprise-4.0.3/plugins/.
    
In the "neo4j.conf" file inside the Neo4j/conf folder add this line:

    dbms.security.procedures.unrestricted=me.tucu.*

Before use:

    // Create full text index for search
    CALL db.index.fulltext.createNodeIndex('fulltext', ['Post','User','Product'], ['status','username','name'])";


Stored Procedures:

    CALL me.tucu.users.get($username);
    CALL me.tucu.users.create($parameters);
    CALL me.tucu.users.profile($username, $username2);
    
    CALL me.tucu.posts.get($username, $limit, $since, $username2);
    CALL me.tucu.posts.create($parameters);
    
    CALL me.tucu.follows.followers($username, $limit, $since);
    CALL me.tucu.follows.following($username, $limit, $since);
    CALL me.tucu.follows.create($username, $username2);
    CALL me.tucu.follows.remove($username, $username2);
    
    CALL me.tucu.mutes.get($username, $limit, $since);
    CALL me.tucu.mutes.create($username, $username2);
    CALL me.tucu.mutes.remove($username, $username2);
    
    CALL me.tucu.likes.get($username, $limit, $since, $username2);
    CALL me.tucu.likes.create($username, $post_id);
    CALL me.tucu.likes.remove($username, $post_id);
    
    CALL me.tucu.timeline.get($username, $limit, $since);
    
    CALL me.tucu.search.get($term, $type, $limit, $offset, $username);
    
    CALL me.tucu.mentions.get($username, $limit, $since, $username2);
    // mentions are automatically created/removed on Post create/remove/update
    
    CALL me.tucu.tags.get($hashtag, $limit, $since, $username);
    // tags are automatically created/removed on Post create/remove/update

Notes
------
     
There are two types of "Post" nodes. Those that promote a "Product" and those that do not. 
The regular posts have direct REPOST relationships, the promoting posts build a tree.        

The likes.remove stored procedure is only in case of user error and only works for 1 minute 
after the likes relationship was created.

Badges
------

[![codecov](https://codecov.io/gh/maxdemarzi/tucume_sprocs/branch/master/graph/badge.svg)](https://codecov.io/gh/maxdemarzi/tucume_sprocs)