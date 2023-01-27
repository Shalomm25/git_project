# Try and Except

'''
You run into errors and try and except alows you to find out what is working or rather if something doesnt work 
you can come up with a contingent to make sure the program still runs
'''

'''
try except blocks allows you to run the code and catch the error without messing with the code or trhowing errors 

'''



try:
    value = 10/0
    # this gives us an invalid input because it isn't a number because you can't divide anything by 0
    number = int(input("Enter a number: "))
    print(number)
except ZeroDivisionError as err:
    # this allows you to catch the specific error
    # the err functoion allows you to print the the error into the program without breaking the flow of the code
    print(err)
except:
    print("Invalid input")
