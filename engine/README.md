# Hemin Engine

Run the tests

    sbt clean compile test

Build and add to local Maven repository:

    sbt clean compile publishM2
    
Run the Engine as a standalone App:

    sbt clean compile run
    
    
## Utils

* MongoDB: __TODO__
* Solr admin: http://localhost:8983/solr/#/
* Neo4j browser: http://localhost:7474/browser/
* Select all Nodes from Neo4j:

```
START n=node(*) RETURN n;
```