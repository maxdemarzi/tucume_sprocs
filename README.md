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

Stored Procedures:

    CALL me.tucu.users.get($username);
    CALL me.tucu.users.create($parameters);