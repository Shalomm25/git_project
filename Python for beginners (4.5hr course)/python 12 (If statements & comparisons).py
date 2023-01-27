# If staetments and comparisons 
# We are going to use comparisons in the if statements instead of boolean values


# pass through three parameters
def max_num(num1,num2,num3):
    if num1 >= num2 and num1 >= num3:
        return num1

    elif num2 >= num1 and num2 >= num3:
        return num2

    else:
        return num3


print(max_num(300,40,5))

# you could also compare strings
# also if you want to compare something tat is equal to use == 
# if you want the comparison to be not equals !=