# Return Statement
# The return  statement allows the function to give us something in return for the function

# If we run this function without a return statementit won't output what we want. It'll just do nothing
def cube(num):
   return num*num*num
# cannot put code after a return statement 

result = cube(4)
print(result)
# The return function gives us the ability to output what we want from the function
# print(cube(3))
# in this case the output will be 27 
