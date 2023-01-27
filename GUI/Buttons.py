'''
Creating Buttons 

'''
from tkinter import *
root = Tk()



# # this is how you can disable the button
# myButton = Button(root, text='Click me!', state=DISABLED)

# # padx makes the button wider
# myButton = Button(root, text='Click me!', padx=50)

# # pady makes the button taller
# myButton = Button(root, text='Click me!',pady=50)

'''
we're going to get the button to do something
'''

def myClick():
    mylabel = Label(root, text="Look! I clicked a button!!")
    mylabel.pack()


myButton = Button(root, text='Click me!', command = myClick)
myButton.pack()


'''
for color codes you could look things up online to figure out how to get specific colors

'''
 
# fg (foreground color) changes the color for the Text
# myButton = Button(root, text='Click me!', command = myClick,fg="blue",bg="red")
# myButton.pack()


# # bg (background color) changes the color of the backround
# myButton = Button(root, text='Click me!', command = myClick,bg="red")
# myButton.pack()


# If you put the parenthesis for the calling the function it'll just run the function
# and it wont let the action of the button do anything 
#                                                            |
#                                                            v
# myButton = Button(root, text='Click me!', command = myClick())

root.mainloop()
