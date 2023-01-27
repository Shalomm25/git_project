lucky_numbers = [4, 8, 15, 16, 23, 42]
freinds = ["Kevin", "Karen", "Jim", "Jim", "Oscar", "Toby"]

print(friends)

# extend function
freinds.extend(lucky_numbers)

# Append adding individual elements onto a list
freinds.append("Creed")

# Insert function
freinds.insert(1, "Kelly")

# clear gets rid of every element in the list
freinds.clear()

# pop take the last element out of the list
freinds.pop()

# to check if a certain element is in the list
print(friends.index["Kevin"])

# counting in the list
print(freinds.count("Jim"))

# sorting in a list
lucky_numbers.sort()

# reverse a list
lucky_numbers.reverse()

# copying lists
friends2 = freinds.copy