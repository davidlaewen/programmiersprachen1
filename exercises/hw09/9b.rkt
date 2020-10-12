#lang racket
(require racket/control)

(define (one-of l)
  (shift k
         (apply append (map (lambda (x) (k x)) l))))

(define (f l1 l2)
  (reset (list (+ (one-of l1) (one-of l2)))))

(f (list 1 2 3) (list 9 12 154))

; 1.
; Given inputs l1 and l2, f computes a list containing the elements of l2 repeated
; |l1|-times, whereby the n-th element in l1 is added to all elements in the n-th
; repetition of l2 in the resulting list.

; 2.
(f (list 1 2 3) (list 9 12 154))
; ~~>
(reset (list (+ (one-of (list 1 2 3)) (one-of (list 9 12 154)))))
; ~~>
(reset (list (+ (shift k (apply append (map (lambda (x) (k x)) (list 1 2 3))))
                (shift k (apply append (map (lambda (x) (k x)) (list 9 12 154)))))))
; ~~>

; The continuation bound by the shift occurence in the left-hand sub-expression contains the call of list and the call of
; + with the right-hand sub-expression. Therefore the shift and reset occurence can be removed by placing x in the same
; context within the mapping function.
; The x's are renamed to improve legibility.
(apply append
       (map
        (lambda (x1) (reset (list (+ x1 (shift k (apply append (map (lambda (x2) (k x2)) (list 9 12 154))))))))
        (list 1 2 3)))
; ~~>

; The final occurences of shift and reset (for the right-hand sub-expression) can be removed by the following
; transformation. The continuation k contained the calls of list and + with x1, placing these within the inner
; mapping function produces the same result.
(apply append
       (map (lambda (x1)
              (apply append
                     (map (lambda (x2)
                            (list (+ x1 x2)))
                          (list 9 12 154))))
            (list 1 2 3)))

; Using apply with append is equivalent to folding with append and the empty list:
(apply append       (list (list 1) (list 2) (list 3)))
(foldr append empty (list (list 1) (list 2) (list 3)))










