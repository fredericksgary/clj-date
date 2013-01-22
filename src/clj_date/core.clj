(ns clj-date.core
  (:import org.joda.time.LocalDate)
  (:refer-clojure :exclude [< > <= >=])
  (:require [clojure.core :as cc]))

(defprotocol IDate
  (-year [date])
  (-month [date])
  (-day [date])
  (-step [date n])
  (-diff [date-1 date-2]))

(defn date? [x] (satisfies? IDate x))

(extend-protocol IDate
  LocalDate
  (-year [ld] (.getYear ld))
  (-month [ld] (.getMonthOfYear ld))
  (-day [ld] (.getDayOfMonth ld))
  (-step [ld n]
    (cond (zero? n) ld
          (pos? n) (.plusDays ld n)
          (neg? n) (.minusDays ld (- n))))
  ;; why is this not built in
  (-diff [ld1 ld2]
    (let [y1 (-year ld1), y2 (-year ld2)]
      (cond
       (cc/< y1 y2)
       (- (-diff ld2 ld1))

       (cc/> y1 y2)
       (let [delta (* 365 (- y1 y2))]
         (+ delta (-diff ld1 (-step ld2 delta))))

       (= y1 y2)
       (let [m1 (-month ld1), m2 (-month ld2)]
         (cond
          (cc/< m1 m2)
          (- (-diff ld2 ld1))

          (cc/> m1 m2)
          (let [delta (* 28 (- m1 m2))]
            (+ delta (-diff ld1 (-step ld2 delta))))

          (= m1 m2) (- (-day ld1) (-day ld2))))))))

(defn date
  "Coerces argument to date"
  ([x]
     (cond (satisfies? IDate x) x

           (vector? x)
           (apply date x)

           :else
           (throw (ex-info "Don't know how to make date from argument." {:arg x}))))
  ([y m d]
     (LocalDate. y m d)))

(def year #(-year (date %)))
(def month #(-month (date %)))
(def day #(-day (date %)))
(def step #(-step (date %1) %2))
(def diff #(-diff (date %1) (date %2)))

(defn <
  ([d] true)
  ([d1 d2] (neg? (diff d1 d2)))
  ([d1 d2 d3 & more]
     (->> (list* d1 d2 d3 more)
          (partition 2 1)
          (every? (fn [[d1 d2]] (< d1 d2))))))

(defn >
  ([d] true)
  ([d1 d2] (pos? (diff d1 d2)))
  ([d1 d2 d3 & more]
     (->> (list* d1 d2 d3 more)
          (partition 2 1)
          (every? (fn [[d1 d2]] (> d1 d2))))))

(defn <=
  ([d] true)
  ([d1 d2] (cc/<= (diff d1 d2) 0))
  ([d1 d2 d3 & more]
     (->> (list* d1 d2 d3 more)
          (partition 2 1)
          (every? (fn [[d1 d2]] (<= d1 d2))))))

(defn >=
  ([d] true)
  ([d1 d2] (cc/>= (diff d1 d2) 0))
  ([d1 d2 d3 & more]
     (->> (list* d1 d2 d3 more)
          (partition 2 1)
          (every? (fn [[d1 d2]] (>= d1 d2))))))

(defn today [] (LocalDate/now))

(defn first-day-of-month
  [d]
  (date (year d) (month d) 1))

(defn last-day-of-month
  ([d] (last-day-of-month (year d) (month d)))
  ([y m]
     (->> (date y m 28)
          (iterate #(step % 1))
          (take-while #(= m (month %)))
          (last))))

;; TODO: this can fail when d > 28...what would useful behavior in
;; that case there be?
(defn month-step
  [the-date n]
  (let [[y m d] ((juxt year month day) the-date)]
    (cond (zero? n)
          the-date

          (or (cc/<= 12 n) (cc/<= n -12))
          (recur (date (+ y (quot n 12))
                       m
                       d)
                 (rem n 12))

          :else
          (let [m' (+ m n)]
            (cond (cc/<= 1 m' 12)
                  (date y m' d)

                  (cc/< m' 1)
                  (date (dec y) (+ m' 12) d)

                  (cc/< 12 m')
                  (date (inc y) (- m' 12) d))))))

(defn inc-month
  "Returns a date one month later than this date (i.e., has the same
  day component)."
  [d]
  (month-step d 1))

(defmethod print-method LocalDate
  [ld pw]
  (.append pw (format "#clj-date/date [%d %d %d]"
                      (year ld)
                      (month ld)
                      (day ld))))

