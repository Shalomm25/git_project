'''

Classes and Objects

classes and objects are usefule in python programming and use all types of data
    strings 
    syntax 
    numbers 
    boolean values

'''
class Student:
    # everything in this wil be part of the class 
    
    def __init__(self, name , major, gpa, on_probation):
        # __init__ helps map out what attibutes the class will have
        # inside the init function it defines what a student is and the class is like a blueprint of what it will be
        self.name = name 
        self.major = major
        self.gpa = gpa 
        # self.on_probation = on_probation
        # this student class is defining what a student is 

 
        