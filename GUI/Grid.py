'''
A grid has columns and rows and they are all called by numbers

'''


from tkinter import *
# This allows us to import everything in tkinter
'''
in kinter everything is a widget

'''
# this is what you do first for tkinter before you do anything else
root = Tk()


# creating a label widget
myLabel1 = Label(root, text="Hello World!")
myLabel2 = Label(root, text="My name is Shalom Makowitz")


'''
The line below tells is how you can do everything but all in one step esspecially 
if there is a lot more code to write that has nothing to do with the GUI
'''
# myLabel1 = Label(root, text="Hello World!").grid(row=0, column=0)
# myLabel2 = Label(root, text="My name is Shalom Makowitz").grid(row=1, column=5)
# myLabel3 =Label(root, text="                           ")

# using the grid widget to place certain things in specific spot on the GUI
myLabel1.grid(row=0, column=0)
myLabel2.grid(row=1, column=5)
# # myLabel3.grid(row=1, column=1 )
# Event loop
root.mainloop()

'''
python is object oriented and you can do all sort of things with python but you 
don't have to write out your code differently

'''



