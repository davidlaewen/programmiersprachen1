#lang racket

(define exception-handler (lambda (msg) (display "Unhandled exception")))

(define (f n)
  (+ 5 (if (zero? n)
           (exception-handler "Division by zero!")
           (/ 8 n))))

(define (g n)
  (+ (f n) (f n)))

(define (h)
  (let/cc k
    (begin
      (set! exception-handler (lambda (msg) (begin (displayln msg) (k))))
      (displayln (g 1))
      (displayln (g 0))
      (displayln (g 2)))))

; ---------------------------------

(define breakpoint false)

(define repl false)

(define (break)
  (let/cc k
    (begin
      (set! breakpoint k)
      (repl))))

(define (continue)
  (breakpoint))

(define (main)
  (display 1)
  (break)
  (display 2)
  (break)
  (display 3))

(let/cc k
  (set! repl k))

; --------------------------------

(define queue empty)

(define (empty-queue?)
  (empty? queue))

(define (enqueue x)
  (set! queue (append queue (list x))))

(define (dequeue)
  (let ((x (first queue)))
    (set! queue (rest queue))
    x))

(define (fork)
  (let/cc k
    (begin
      (enqueue (lambda () (k 1)))
      0)))

(define (join)
  (if (not (empty-queue?))
      ((dequeue))
      'alljoined))

(define (yield)
  (let/cc k
    (enqueue k)
    ((dequeue))))


(define (fac n) (if (zero? n) 1 (* n (fac (- n 1)))))
(define (fib n) (if (< n 2) 1 (+ (fib (- n 1)) (fib (- n 2)))))

(define (printfibs n)
  (if (zero? n)
      (begin (print "Fibonacci done") (newline))
      (begin (print (format "Fib(~A)=~A" n (fib n)))
             (newline)
             (yield)
             (printfibs (- n 1)))))

(define (printfacs n)
  (if (zero? n)
      (begin (print "Factorial done") (newline))
      (begin (print (format "Fac(~A)=~A" n (fib n)))
             (newline)
             (yield)
             (printfacs (- n 1)))))

(define (test-forkjoin)
  (if (= (fork) 0)
      (printfibs 4)
      (printfacs 8))
  (join))






