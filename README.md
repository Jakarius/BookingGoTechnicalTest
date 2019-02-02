# BookingGoTechnicalTest

## Part 1

### Dave API results
Run `java DaveResults.jar [pickup_lat] [pickup_long] [dropoff_lat] [dropoff_long]`.
This program returns the results of the Dave supplier API in descending price order.

### Filtered by number of passengers
Run `java PassengerFilter.jar [pickup_lat] [pickup_long] [dropoff_lat] [dropoff_long] [number_of_passengers]`.
This program returns the cheapest supplier for each car type, filtered so that all results will have sufficient passenger space.

## Part 2 - REST API
Run `java Server.jar [port_number]` (port number defaults to 8000 if none specified).
Then make a HTTP request through a web browser or otherwise.
Example URL: `http://localhost:8000/taxiREST?passengers=2&pickup=51.470020,-0.454295&dropoff=51.440020,-0.464295`
