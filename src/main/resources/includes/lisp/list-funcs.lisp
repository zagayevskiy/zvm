(defun list-reverse-rec (source reversed)
    (cond
        ( (nil? source) reversed )
        ( T (list-reverse-rec (cdr source) (cons (car source) reversed)))
    )
)

(defun list-reverse (source) (list-reverse-rec source nil))