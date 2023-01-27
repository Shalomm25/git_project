'''
Creating Input Fields

'''
from tkinter import *
root = Tk()

# e = Entry(root,width=50)
# e.pack()


# INSERT
e = Entry(root,width=50)
e.pack()
e.insert(0, "Enter you name: ")

# # width
# e = Entry(root,width=50)
# e.pack()

# # color
# e = Entry(root,fg="Red",bg="yellow")
# e.pack()


# # border width
# e = Entry(root, borderwidth=5)
# e.pack()


'''
how do we do stuff with this function
'''

def myClick():
    hello = "Hello " + e.get()
    # the get() function helps get the fucntion we created
    mylabel = Label(root, text=hello)
    mylabel.pack()


myButton = Button(root, text='Enter your name:', command = myClick)
myButton.pack()




root.mainloop()
