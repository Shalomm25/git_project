'''
Build a calculator
'''

from tkinter import *
root = Tk()

# for the title of the gui window
root.title('Simple Calculator')



e = Entry(root,width=35, borderwidth=5)
e.pack()

# columnspan is how many columns widget occupies
# padx is wider 
# pady is taller
e.grid(row=0, column=0, columnspan=3,padx=10,pady=10)


# This function is alowing each button to have a value
def button_click(number):
    # this delete functioon will delete everything in the text box once you click a different button
    # e.delete(0, END)
    # we need to insert the number we click + another number that adds onto the text from left to right
    # create a variable that adds what was currently in there and what new number we will put in
    current = e.get()
    # unfortunately after the varuable, now the program repeats every thing that was perviously in there 
    # for we need to delete what was in there
    e.delete(0, END)

    e.insert(0, str(current) + str(number))


# clear buttom 
def button_clear():
        e.delete(0, END)

# add button
# adding globals to be able to keep numbers in the program
def button_addition():
    first_number = e.get()
    global f_num
    global math
    math = "addition"
    f_num = int(first_number)
    e.delete(0, END)


def button_equal():
    second_number = e.get()
    e.delete(0 , END)
    
    
    if math == "addition":    
        e.insert(0, f_num + int(second_number))

    elif math == "subtraction":
        e.insert(0, f_num - int(second_number))

    elif math == "multiplication":    
        e.insert(0, f_num / int(second_number))

    elif math == "division":
        e.insert(0, f_num * int(second_number))




def button_subtraction():
    first_number = e.get()
    global f_num
    global math
    math = "subtraction"
    f_num = int(first_number)
    e.delete(0, END)


def button_multiplication():
    first_number = e.get()
    global f_num
    global math
    math = "multiplication"
    f_num = int(first_number)
    e.delete(0, END)



def button_division():
    first_number = e.get()
    global f_num
    global math
    math = "division"
    f_num = int(first_number)
    e.delete(0, END)



'''
lambda - A lambda function is an anonymous function (i.e., defined without a name) that can take any number of arguments but,
unlike normal functions, evaluates and returns only one expression.
'''


# Nummber buttons
# and +,=
button_1 = Button(root, text="1", padx=40, pady=20, command=lambda: button_click(1))
button_2 = Button(root, text="2", padx=40, pady=20, command=lambda: button_click(2))
button_3 = Button(root, text="3", padx=40, pady=20, command=lambda: button_click(3))
button_4 = Button(root, text="4", padx=40, pady=20, command=lambda: button_click(4))
button_5 = Button(root, text="5", padx=40, pady=20, command=lambda: button_click(5))
button_6 = Button(root, text="6", padx=40, pady=20, command=lambda: button_click(6))
button_7 = Button(root, text="7", padx=40, pady=20, command=lambda: button_click(7))
button_8 = Button(root, text="8", padx=40, pady=20, command=lambda: button_click(8))
button_9 = Button(root, text="9", padx=40, pady=20, command=lambda: button_click(9))
button_0 = Button(root, text="0", padx=40, pady=20, command=lambda: button_click(0))
button_add = Button(root, text="+", padx=39, pady=20, command=button_addition)
button_equal = Button(root, text="=", padx=91, pady=20, command=button_equal)
button_clear = Button(root, text="clear", padx=79, pady=20, command=button_clear)

button_subtract = Button(root, text="-", padx=41, pady=20, command=button_subtraction)
button_multiply = Button(root, text="*", padx=40, pady=20, command=button_division)
button_divide = Button(root, text="/", padx=40, pady=20, command=button_multiplication)

## put buttons on screen
# row 3
button_1.grid(row=3, column=0)
button_2.grid(row=3, column=1)
button_3.grid(row=3, column=2)
# row 2
button_4.grid(row=2, column=0)
button_5.grid(row=2, column=1)
button_6.grid(row=2, column=2)
# row 1
button_7.grid(row=1, column=0)
button_8.grid(row=1, column=1)
button_9.grid(row=1, column=2)
# row 4
button_0.grid(row=4, column=0)
button_clear.grid(row=4, column=1,columnspan=2)
# row 5
button_add.grid(row=5, column=0)
button_equal.grid(row=5, column=1,columnspan=2)
# row 6
button_subtract.grid(row=6, column=0)
button_multiply.grid(row=6, column=1)
button_divide.grid(row=6, column=2)



root.mainloop()