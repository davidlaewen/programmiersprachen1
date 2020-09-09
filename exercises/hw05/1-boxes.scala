With(fun = λx.(x+0)):
	((fun 0) + (fun 0))
	
~~> 0
	
With(fun = λx.(x+0)):
	With(x = (fun 0)):
		(x + x)
		
		
With(x = (λx.(x+0) 0)):
	(x + x)
		
~~> 0

Question 1: Yes
Question 2: Yes

-----------------------

Question 3: Yes
Question 4: Yes
Question 5: No 
- counter-example:

val bcfaeProgram1 =
	wth("counter", NewBox(0),
		wth("fun", 
			Fun("x", 
				Seq(SetBox("counter", Add(1, OpenBox("counter"))),
					OpenBox("counter"))),
			Add(App("fun", 0), App("fun", 0))))
			
~~> 3
	  
val bcfaeProgram2 =
	wth("counter", NewBox(0),
		wth("fun", 
			Fun("x", 
				Seq(SetBox("counter", Add(1, OpenBox("counter"))),
				OpenBox("counter"))),
			wth("x", App("fun", 0),
				Add("x", "x"))))
		 
~~> 2
		 