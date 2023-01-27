# If statements
# a special strcture in python that helps the program make decisions
# allow the program to respond to the input 
# It makes the program smarter


is_male = True
# is_male = False
is_tall = True

# if this than this otherwise this

if is_male or is_tall:
    print("You are male or tall or both")

else:
    print("you are niether male or tall")



if is_male and is_tall:
    print("You are a tall male")
elif is_male and not(is_tall):
    print("You a short male)
elif not(is_male) and not(is_tall):
    print("You are not a male and not tall")
elif not(is_male) and is_tall:
    print("You are not male but you are tall")
else:
    print("you are niether male nor tall")