
* Make sure that the 4567 port is available.

* To execute the tests, run:

 $ mvn test

* To compile and start the server, run:

 $ mvn clean install

 $ java -jar target/videostats-1.0-jar-with-dependencies.jar

  * You can now view the statistics at http://127.0.0.1:4567/statistics

  * You can also post to the API or call delete using the "test.py" script.
    * Example (with duration=30.5)

      $ python test.py 30.5

    * Example (delete)

      $ python test.py --delete

    * Obs: the script depends on requests>=2.22.0.
