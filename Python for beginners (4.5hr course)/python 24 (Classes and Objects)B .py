'''
an object is the actual thing that we're making the class for 
in this case it's a student

in a different file 
from Student in Student

'''
from Student_24 import Student

student1 = Student("Jim", "Business", 3.1, False)
student2 = Student("Jeff", "CIS", 2.0, True)

print(student1.gpa)
print(student2.major)