'''
The beginning
'''


from tkinter import *
# This allows us to import everything in tkinter
'''
in kinter everything is a widget

'''
# this is what you do first for tkinter before you do anything else
root = Tk()


# creating a label widget
myLabel = Label(root, text="Hello World!")


# putting it onto the screen
myLabel.pack()


# Event loop
root.mainloop()
