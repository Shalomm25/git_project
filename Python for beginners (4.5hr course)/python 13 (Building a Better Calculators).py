# Building a better calculator

# add, subtract, multiply and divide

# input from the user
# Three variables
 
num1 = float(input("Enter first number: "))
operator = input("Enter Operator: ")
num2 = float(input("Enter second number: "))


if operator == "+":
    print(num1 + num2)

elif operator == "-":
    print(num1-num2)

elif operator == "/":
    print(num1/num2)

elif operator == "*":
    print(num1*num2)
else:
    print("invalid operator")

