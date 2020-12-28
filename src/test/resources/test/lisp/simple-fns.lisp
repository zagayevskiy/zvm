(def! plus (fn* (x y) (+ x y)))
(defun! mul (x y) (* x y))
(plus 100 (mul 5 ((\* (x y) (+ x y)) 1 1)))