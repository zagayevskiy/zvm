(def! reverse
    (fn* (src)
        (let*
            (reverse-rec (fn* (source accum)
                            (cond
                                ((nil? source) accum)
                                (T (reverse-rec (cdr source) (cons (car source) accum)))
                            )
                         )
            )
            (reverse-rec src nil)
        )
    )
)

(def! range
    (fn* (from to step)
        (let*
            (
                rec-step (cond ((number? step) step) (T 1))
                @finish (cond ((< from to) <) ((> from to) >))
                @next (cond ((< from to) -) ((> from to) +))

                range-rec (fn* (current accum)
                            (cond
                                ((@finish current from) accum)
                                (T (range-rec (@next current rec-step) (cons current accum)))
                            )
                          )
            )
            (cond
                ((< from to) (range-rec (+ from (* (/ (- to from 1) rec-step) rec-step)) nil))
                ((> from to) (range-rec (- from (* (/ (- from to 1) rec-step) rec-step)) nil))
                (T nil)
            )
        )
    )
)

(range 1 10) ; 1 2 3 4 5 6 7 8 9
(range 10 1) ; 10 9 8 7 6 5 4 3 2
(range 1 10 3) ; 1 4 7
(range 0 10 2) ; 0 2 4 6 8
(range 10 1 2) ; 10 8 6 4 2

; TODO use includes and arguments
(reverse (list 1 2 3))