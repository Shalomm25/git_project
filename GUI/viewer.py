'''

Build an Image Viewer App

'''

from tkinter import *
from PIL import ImageTk, Image


root = Tk()
root.title("Lear To Code at codemy")

# root.iconbitmap("C:\\Users\\smako\\Desktop\\Python practice\\GUI\\ famjam.ico")


'''
I'm having an issue with these lines of code
||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
'''
# my_img1 = ImageTk.PhotoImage(Image.open("images\\Middle_Finger.png"))
# my_img2 = ImageTk.PhotoImage(Image.open("images\\Middle_Finger2.png"))
# my_img3 = ImageTk.PhotoImage(Image.open("images\\famjam.jpg"))
# my_img4 = ImageTk.PhotoImage(Image.open("images\me1.jpg"))
# my_img5 = ImageTk.PhotoImage(Image.open("images\\me2.jpg"))
# my_img6 = ImageTk.PhotoImage(Image.open("images\\me3.jpg"))


my_img1 = ImageTk.PhotoImage(Image.open("C:\Users\smako\Desktop\Python practice\GUI\images\Middle_Finger.png"))
my_img2 = ImageTk.PhotoImage(Image.open("C:\Users\smako\Desktop\Python practice\GUI\images\Middle_Finger2.png"))
my_img3 = ImageTk.PhotoImage(Image.open("C:\Users\smako\Desktop\Python practice\GUI\images\ famjam.jpg"))
my_img4 = ImageTk.PhotoImage(Image.open("C:\Users\smako\Desktop\Python practice\GUI\images\me1.jpg"))
my_img5 = ImageTk.PhotoImage(Image.open("C:\Users\smako\Desktop\Python practice\GUI\images\me2.jpg"))
my_img6 = ImageTk.PhotoImage(Image.open("C:\Users\smako\Desktop\Python practice\GUI\images\me3.jpg"))

'''

I need to figure out how to get this to work. for some reason the file won't work

'''


image_list = [my_img1, my_img2, my_img3, my_img4, my_img5, my_img6]

image_list[2]


my_label =  Label(image=my_img1)
my_label.grid(row=0, column=0, cloumnspan=3)


def forward():
    return


def back():
    return


button_back = Button(root, text='<<')
button_exit = Button(root, text='EXIT PROGRAM', command=root.quit)
button_forward = Button(root, text='>>')

button_back.grid(row=1, column=0)
button_exit.grid(row=1, column=2)
button_forward.grid(row=1,column=3)


# button_quit = Button(root, text="Exit Program", command=root.quit)
# button_quit.pack()


root.mainloop()


