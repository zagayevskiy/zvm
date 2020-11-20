(def! f (fn* (x) (cond
    ((= x 1000) x)
    (T (f (+ x 1))))))

(f 0)