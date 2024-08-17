### Description
This is a command-line Java application that will generate a tool rental agreement for a given set of checkout criteria.

### Prerequisite to running
- Run `mvn install` to install all dependencies and generate the necessary jar file.
  - This also runs the tests

### How to run
The application supports **Checkout** and **View** operations. Both offer short and long-form CLI options:

#### Checkout
- To checkout a tool: `java -jar target/app-1.0-SNAPSHOT-jar-with-dependencies.jar <checkout> <toolcode> <rentaldaycount> <discountpercent> <checkoutdate>`
    - Short-form example: `java -jar target/app-1.0-SNAPSHOT-jar-with-dependencies.jar -c -tc JAKD -rd 4 -dp 10 -cd 9/3/15`
    - Long-form example: `java -jar target/app-1.0-SNAPSHOT-jar-with-dependencies.jar --checkout --tool-code JAKD --rental-days 4 --discount-percent 10 --checkout-date 9/3/15`

#### View
_Note_: This option was added to support the requirement that rental agreements should be viewable at a later point in time, per response to emailed questions.

- To view a previously generated rental agreement: `java -jar target/app-1.0-SNAPSHOT-jar-with-dependencies.jar <view> <agreement-id>`
  - Short-form example: `java -jar target/app-1.0-SNAPSHOT-jar-with-dependencies.jar -v -id 1`
  - Long-form example: `java -jar target/app-1.0-SNAPSHOT-jar-with-dependencies.jar --view --agreement-id 1`

#### Command Line Options
```
usage: app [-c] [-tc -rd -dp -cd] | app [-v] [-id]

Usage guidelines:
  -c, -tc, -rd, -dp, -cd must be used together.
  -v, -id must be used together.
  
 -c,--checkout                  Perform a checkout
 -cd,--checkout-date <arg>      Required for checkout: Checkout date (format of mm/dd/yy)
 -dp,--discount-percent <arg>   Required for checkout: Discount percent (number, from 0-100)
 -rd,--rental-days <arg>        Required for checkout: Number of rental days (number, minimum of 1)
 -tc,--tool-code <arg>          Required for checkout: Tool code
 
 -v,--view                      View a rental agreement
 -id,--agreement-id <arg>       Required for view: id of rental agreement to
                                view
```

### Main technologies used:

- SQLite, to provide persistent storage between application runs
- JDBI, to support database interactions
- Flyway, for DB migrations / setup
- Apache Commons CLI, to provide a cleaner CLI experience
