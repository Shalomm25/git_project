# Dictionaries

monthConversions = {
    "Jan":"January",
    "Feb": "February",
    "Mar": "March",
    "Apr": "April",
    "May": "May",
    "Jun": "June",
    "Jul": "July",
    "Aug": "August",
    "Sep": "September",
    "Oct": "October",
    "Nov": "November",
    "Dec": "December",
}

# print(monthConversions["Nov"])
# the output will be the full name of the abreviation from the dictionary
print(monthConversions.get("Dec"))
# the .get function allows us to know when we've asked for the wrong dictionary it'll output none
# without .get it'll say not a valid key