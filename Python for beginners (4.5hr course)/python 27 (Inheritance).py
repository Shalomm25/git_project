'''

Inheritance

inheritance is when you have a class with a bunch of attributes and you create another class that inherites 
the attributes of the previous class 

'''


from Chef_27 import Chef
from ChineseChef_27 import ChineseChef

myChef = Chef()

myChef.make_special_dish()

myChineseChef = ChineseChef()
myChineseChef.make_special_dish()
myChineseChef.make_fried_rice()
