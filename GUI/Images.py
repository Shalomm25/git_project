from tkinter import *
from PIL import ImageTk, Image


root = Tk()
root.title("Lear To Code at codemy")

# root.iconbitmap("C:\Users\smako\Desktop\Python practice\GUI/Middle_Finger.ico")



my_img = ImageTk.PhotoImage(Image.open("Middle_Finger.png"))

my_label =  Label(image=my_img)

my_label.pack()







button_quit = Button(root, text="Exit Program", command=root.quit)
button_quit.pack()






root.mainloop()