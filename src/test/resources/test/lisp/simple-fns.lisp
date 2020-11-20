(def! plus (fn* (x y) (+ x y)))
(def! mul (fn* (x y) (* x y)))
(plus 100 (mul 5 2))