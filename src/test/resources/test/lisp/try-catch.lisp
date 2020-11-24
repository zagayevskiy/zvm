(def! a (fn* (x) (throw! 3)))
(def! b (fn* (x) (+ 1 (a x))))
(def! c (fn* (x) (+ 1 (b x))))
(def! d (fn* (x) (try* (c 2) (catch* e 100500))))

(d 248)