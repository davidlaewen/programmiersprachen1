#lang racket
(require racket/control)

(define (one-of l)
  (shift k
         (apply append (map (lambda (x) (k x)) l))))
         
(define (f l1 l2)
  (reset (list (+ (one-of l1) (one-of l2)))))

(f (list 1 2 3) (list 9 12 154))




; Stepwise reduction:
(f (list 1 2 3) (list 9 12 154))
; ~~>
(reset (list (+ (one-of (list 1 2 3)) (one-of (list 9 12 154)))))
; ~~>
(reset (list (+ (shift k
                       (apply append (map (lambda (x) (k x)) (list 1 2 3))))
                (shift k
                       (apply append (map (lambda (x) (k x)) (list 9 12 154)))))))
; ~~>
; (reset (lambda (k) (apply append (map (lambda (x) (k x)) (list 1 2 3)))))

(define (one-of* l)
  (shift k
         (apply append (map (lambda (x) (k x)) l))))
         
(define (f* l1 l2)
  (reset (list (one-of l1) (one-of l2))))

(f* (list 1 2 3) (list 10 20 30))

