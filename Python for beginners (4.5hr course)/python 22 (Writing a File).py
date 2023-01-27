'''

writing a file 

writing and appending to files in python
adding more to a file 

'''

employee_file = open("employees.txt", "a")
# the a is for append in appending to a file

# employee_file = open("employees1.txt", "w")
# the w for write allows us to make a new file


employee_file.write("Toby - Human Resources")
# this is allowing us to append a line to the file
# but if you run it a bunch of times it'll just add the same thing to the file again
# the output would be Toby - Human ResourcesToby - Human Resources
employee_file.write("\nKelly - Customer Service")
# \n adds a new line to the file

employee_file.close




