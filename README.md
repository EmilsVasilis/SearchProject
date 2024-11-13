# SearchProject

To build

```
mvn clean install
```

To run
```
sudo mvn exec:java -Dexec.mainClass="org.search.Main"
```

To get trec_eval
```
./trec_eval src/main/resources/qrels.assignment2.part1 src/main/resources/output.txt
```