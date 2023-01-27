# 2D lists and nested loops


# 2D lists
Number_grid = [
    #0 #1 #2
    [1,2,3], #0
    [4,5,6], #1
    [7,8,9], #2
    [0]
]


# the first square bracket is s the row and the second is the column
# The output is 1 because it is the first number in column 0 and row 0
# print(Number_grid[0][0])



# Nested for loop
# a for loop inside of a for looop
for row in Number_grid:
    for col in row:
        print(col)


