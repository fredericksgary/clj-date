# clj-date

Time is complicated. Sometimes you just want dates.

`clj-date` is this thing I just hacked out of another project.  It
uses joda for date math (though this could be removed as a strict
requirement), and provides a protocol for dates as well as some basic
functions for handling dates.

## What? Why isn't this a time library?

Because it considers a date to be something with an integral year,
month, and day, and completely ignores time zones and partial days or
whatever else. If you're handling days as discrete units, then this
approach will probably work.

## Usage

``` clojure
(require '[clj-date.core :as d])

(d/step [2013 1 22] 7)
;; => #clj-date/date [2013 1 29]
(type *1)
;; => org.joda.time.LocalDate

;; then do other things
```

## License

Copyright © 2013 Gary Fredericks

Distributed under the Eclipse Public License, the same as Clojure.
