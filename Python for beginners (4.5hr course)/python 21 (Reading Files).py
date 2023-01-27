
'''

Reading files

reading from existing files

'''

employee_file = open("Name of file.txt", "r")
# relative path to the file or file path
# r - read - just reads the file 
# w - write - changes the file write new info or change the info 
# a - append - append info to the end if the file
# r+ - read and write - all the power to read and write


print(employee_file.readable())
# readable lets us know if we can read the file 


print(employee_file.readline())
print(employee_file.readline())
# readline just reads a line from the file 
# if we do it twice it will read the first two lines ect...

print(employee_file.readline()[1])
# this will print the second line of the file because the the index is 1


print(employee_file.readlines())
# readlines takes every line in the file and turns it into an list

for employee in employee_file.readlines():
    print(employee)
# this helps with reading every line in the file using a for loop


employee_file.close()
# you have to close the file everytime you open it or once you open it 


