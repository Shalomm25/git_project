# Exponent Function


# inside of the function we are takining in two pieces of information and base number and a power number
# The base number is being taken to the power number
# we define a variable called result and result is going to specify the amoount
# everytime we loop we are multiplying result by the base number
# in the edn of the loop we are printing out the result


def raise_to_power(base_num, pow_num):
    result = 1 
    for index in range(pow_num):
        result = result * base_num
    return result

print(raise_to_power(2,3))