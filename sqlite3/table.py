''''
Building a table to put data int
'''



import sqlite3

conn = sqlite3.connect('customer.db')



''''
create a cursor
a cursor tells the data base what you want to do
'''
# creat cursor
c =  conn.cursor()

# create a table
# execute() command allows us to do an action with the cursor


'''
WE  need to create a table using dock strings """" """
'''
''''
ALL THE SQL COMMANDS NEED TO BE IN CAPS
'''

c.execute("""CREATE TABLE customers (
    first_name text,
    last_name text,
    email text
)""")


''''
There are only 5 data types to choose from in sqlite3
NULL - DOES IT EXIST OR DOES IT NOT EXIST
INTEGER - WHOLE NUMBER
REAL - DECIMAL OR FLOAT 
TEXT - TEXT 
BLOB - IMAGE, MP3 FILE ECT
'''


# commit our command
conn.commit()

# close our connection
conn.close()
