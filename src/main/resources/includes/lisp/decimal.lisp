(include! "list-funcs.lisp")

(def! decimal-base 10000)
(defun decimal (int) (cond
    ((number? int) (list-reverse (split-int int nil)))))

(defun split-int (int acc)
    (cond
        ( (< int decimal-base) (cons int acc))
        ( T (split-int (/ int decimal-base) (cons (% int decimal-base) acc)))
    )
)

(defun decimal+finish-rec (x transfer-int acc)
    (cond
        ( (nil? x) (cond
                        ( (= transfer-int 0) acc)
                        ( T (cons transfer-int acc))
                    )
        )
        ( T (decimal+finish-rec
                (cdr x)
                (/ (+ transfer-int (car x)) decimal-base)
                (cons (% (+ transfer-int (car x)) decimal-base) acc))
        )
    )
)

(defun decimal+rec (left right transfer-int acc)
    (cond
        ( (nil? left) (decimal+finish-rec right transfer-int acc))
        ( (nil? right) (decimal+finish-rec left transfer-int acc))
        ( T (decimal+rec
                (cdr left)
                (cdr right)
                (/ (+ transfer-int (car left) (car right) ) decimal-base)
                (cons (% (+ transfer-int (car left) (car right) ) decimal-base) acc) )
        )

    )
)

(defun decimal+ (left right) (list-reverse (decimal+rec left right 0 nil)))

(defun fib-decimal-rec (prev cur n) (cond ((= n 0) prev) (T (fib-decimal-rec cur (decimal+ prev cur) (- n 1)))))
(defun fib-decimal (n) (fib-decimal-rec (decimal 0) (decimal 1) n))

(defun count-digits-rec (n acc)
    (cond
        ( (< n 10) acc)
        ( T (count-digits-rec (- n 1) (+ acc 1)))
    )
)
(defun count-digits (n) (count-digits-rec n 1))

(defun decimal-digits-count-rec (d acc)
        (cond
            ( (nil? (cdr d)) (+ acc (count-digits (car d))))
            ( T (decimal-digits-count-rec (cdr d) (+ acc 4)))
        )
)
(defun decimal-digits-count (d) (decimal-digits-count-rec 0) )